(ns reg.framework.client.core
  (:require [clojure.walk :refer [walk]]
            [cljs.core.async :refer [chan put!]]
            [cljs.nodejs]
            [cognitect.transit :as transit]
            [reagent.core :as r])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(def ^:private -ipc (.-ipcRenderer (cljs.nodejs/require "electron")))
(def content (r/atom [:div.no-content]))
(def events (chan))

(defn symbol->function [component-map form]
  (cond (not (vector? form)) form
        (not (symbol? (first form))) form
        :else (let [k (keyword (name (first form)))
                    component (get component-map k)]
                (if component
                  (into [component] (drop 1 form))
                  (throw (js/Error. (str "Could not find component for symbol " (first form))))))))

(defn symbols->functions [component-map form]
  (walk (partial symbol->function component-map) identity form))

(defn element-event-handlers->functions [form]
  (cond (not (vector? form)) form
        (not (map? (second form))) form
        :else (let [[element options] form
                    listeners (into {} (for [k (filter #(.startsWith (name %) "on-") (keys options))]
                                [k #(do
                                      (put! events (get options k))
                                      (when (get-in options [k :prevent-default])
                                        (.preventDefault %))
                                      true)]))]
                (into [element listeners] (drop 2 form)))))

(defn all-event-handlers->functions [form]
  (walk element-event-handlers->functions identity form))

(defn content-renderer [component-map]
  (->> @content
       (symbols->functions component-map)
       (all-event-handlers->functions)))

(defn start [component-map mount-point]
  (r/render-component [content-renderer component-map] mount-point)
  (.on -ipc "render" (fn [event data]
                       (let [r (transit/reader :json)]
                         (reset! content (transit/read r data)))))
  (go-loop []
    (when-let [e (<! events)]
      (let [w (transit/writer :json)
            data (transit/write w e)]
        (.send -ipc "renderer-event" data))
      (recur))))