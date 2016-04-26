(ns reg.application.http
  (:require [cljs.core.async :refer [promise-chan timeout >! <! close!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))


(defn login [credentials]
  (let [resp-chan (promise-chan)
        resp {:status :success :data {:token "123456"}}]
    (go
      (<! (timeout 1000))
      (>! resp-chan resp)
      (close! resp-chan))
    resp-chan))
