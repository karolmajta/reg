(ns reg.framework.electron.window-manager
  (:require [cljs.nodejs]
            [cljs.core.async :refer [put! chan close!  <! >!]]
            [com.stuartsierra.component :refer [Lifecycle]]
            [cognitect.transit :as transit])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(def ^:private -BrowserWindow (.-BrowserWindow (cljs.nodejs/require "electron")))
(def ^:private -ipc (.-ipcMain (cljs.nodejs/require "electron")))
(def path (cljs.nodejs/require "path"))

(defn- create-window [options]
  (-BrowserWindow. (clj->js options)))

(defprotocol IWindowManager
  (open [this key options])
  (close [this key])
  (on [this key event-name f])
  (off [this key event-name])
  (send-hiccup [this key hiccup-form]))

(defrecord WindowManager [windows events]
  Lifecycle
  (start [this]
    (.on -ipc "renderer-event" (fn [event data]
                                  (let [r (transit/reader :json)
                                        e (transit/read r data)]
                                    (put! (:events this) e))))
    this)

  IWindowManager

  (open [this key options]
    (let [window (create-window options)
          tx (chan)
          rx (chan)]

      (.loadURL window (str "file://" (.join path js/BASE_PATH "resources" (str "index.html?" (name key)))))
      (let [web-contents (.-webContents window)]
        (.on web-contents "dom-ready" (fn [_]
                                        (go-loop []
                                          (when-let [hiccup-form (<! tx)]
                                            (let [data (transit/write (transit/writer :json) hiccup-form)]
                                              (try
                                                (.send web-contents "render" (str data))
                                                (catch js/Error _))
                                              (recur))))))

      (swap! (:windows this) assoc key {:window window :tx tx :rx rx}))))

  (close [this key]
    (let [window (get-in @(:windows this) [key :window])
          tx (get-in @(:windows this) [key :tx])
          rx (get-in @(:windows this) [key :rx])]
      (close! tx)
      (close! rx)
      (.close window)
      (swap! (:windows this) dissoc key)))

  (on [this key event-name f]
    (let [window (get-in @(:windows this) [key :window])]
      (.on window event-name f)))

  (off [this key event-name]
    (let [window (get-in @(:windows this) [key :window])]
      (.removeAllListeners window event-name)))

  (send-hiccup [this key hiccup-form]
    (let [tx (get-in @(:windows this) [key :tx])]
      (put! tx hiccup-form))))

(defn create []
  (map->WindowManager {:windows (atom {})}))