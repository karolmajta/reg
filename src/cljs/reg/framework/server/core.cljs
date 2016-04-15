(ns reg.framework.server.core
  (:require [cljs.core.async :refer [<! chan]]

            [com.stuartsierra.component :as component]

            [reg.framework.electron.app]
            [reg.framework.electron.window-manager]
            [reg.framework.reconciler.core])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))


(defn create-framework []
  (component/system-map
    :events (chan)
    :window-manager (component/using
                      (reg.framework.electron.window-manager/create)
                      [:events])
    :reconciler (component/using
                  (reg.framework.reconciler.core/create)
                  [:window-manager :events])))



(defn start [handler-fn]
  (let [system (component/start (create-framework))]

    (go-loop []
             (let [e (<! (:events system))]
               (handler-fn e)
               (recur)))

    #(let [rendered-value %]
      (reg.framework.reconciler.core/reconcile (:reconciler system) rendered-value))))
