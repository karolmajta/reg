(ns reg.framework.reconciler.window
  (:require [reg.framework.electron.window-manager]
            [reg.framework.reconciler.reconcile]
            [reg.framework.reconciler.utils :refer [bind-events-on-add
                                                    unbind-events-on-remove]]))


(defmethod reg.framework.reconciler.reconcile/reconcile-add :window [context previous next]
  (println "RECONCILE WINDOW ADD")
  (reg.framework.electron.window-manager/open (:window-manager context) (:key (second next)) {})
  (bind-events-on-add
    (:events context) previous next
    #(reg.framework.electron.window-manager/on (:window-manager context) (:key (second next)) %1 %2)))


(defmethod reg.framework.reconciler.reconcile/reconcile-remove :window [context previous next]
  (println "RECONCILE WINDOW REMOVE")
  (unbind-events-on-remove
    (:events context) previous next
    #(reg.framework.electron.window-manager/off (:window-manager context) (:key (second previous)) %1))
  (reg.framework.electron.window-manager/close (:window-manager context) (:key (second previous))))


(defmethod reg.framework.reconciler.reconcile/reconcile-update :window [context previous next]
  (println "RECONCILE WINDOW UPDATE"))

(defmethod reg.framework.reconciler.reconcile/extended-context :window [context previous next]
  (merge context {:window-key (:key (second (or next previous)))}))