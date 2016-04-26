(ns reg.framework.electron.app
  (:require [cljs.nodejs]
            [cljs.core.async :refer [put!]]))

(def ^:private -electron-app (.-app (cljs.nodejs/require "electron")))

(.on -electron-app "window-all-closed" #(.preventDefault %))

(defn quit [] (.quit -electron-app))
(defn on [event-name f] (.on -electron-app event-name f))
(defn off [event-name] (.removeAllListeners -electron-app event-name))
