(ns reg.framework.client.core
  (:require [clojure.walk :refer [prewalk]]
            [cljs.core.async :refer [chan put!]]
            [cljs.nodejs]
            [cognitect.transit :as transit]
            [reagent.core :as r])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(def ^:private -ipc (.-ipcRenderer (cljs.nodejs/require "electron")))
(def content (r/atom [:div.no-content]))
(def events (chan))

(defn make-listeners [options]
  (into {} (for [k (filter #(.startsWith (name %) "on-") (keys options))]
             [k #(do
                  (put! events (assoc (get options k) :event-data %))
                  (when (get-in options [k :prevent-default])
                    (.preventDefault %))
                  true)])))

(defn data-form->function-form [component-map form]
  (cond (not (vector? form)) form
        (not (symbol? (first form))) form
        :else (let [k (keyword (name (first form)))
                    component (get component-map k k)
                    options (second form)
                    h (if (map? options) [component (merge options (make-listeners options))] [component options])
                    t (drop 2 form)]
                (if component
                  (into [] (concat h t))
                  (throw (js/Error. (str "Could not find component for symbol " (first form))))))))

(defn data-forms->function-forms [component-map form]
  (prewalk (partial data-form->function-form component-map) form))

(defn content-renderer [component-map]
  (data-forms->function-forms component-map @content))

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
