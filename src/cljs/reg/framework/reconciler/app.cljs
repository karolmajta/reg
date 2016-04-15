(ns reg.framework.reconciler.app
  (:require [reg.framework.electron.app]
            [reg.framework.reconciler.reconcile]
            [reg.framework.reconciler.utils :refer [bind-events-on-add
                                                    unbind-events-on-remove]]))


(defmethod reg.framework.reconciler.reconcile/reconcile-add :app [context previous next]
           (println "RECONCILE APP ADD")
           (bind-events-on-add (:events context) previous next reg.framework.electron.app/on))


(defmethod reg.framework.reconciler.reconcile/reconcile-remove :app [context previous next]
           (println "RECONCILE APP REMOVE")
           (unbind-events-on-remove (:events context) previous next reg.framework.electron.app/off)
           (reg.framework.electron.app/quit))


(defmethod reg.framework.reconciler.reconcile/reconcile-update :app [context previous next]
           (println "RECONCILE APP UPDATE"))
