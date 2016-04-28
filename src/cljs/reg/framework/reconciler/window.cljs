(ns reg.framework.reconciler.window
  (:require [reg.framework.electron.window-manager]
            [reg.framework.reconciler.reconcile]
            [reg.framework.reconciler.utils :refer [bind-events-on-add
                                                    unbind-events-on-remove]]))


(defmethod reg.framework.reconciler.reconcile/reconcile-add :window [context previous next]
  (let [opts (second next)
        {:keys [key maximized]} opts]
  (reg.framework.electron.window-manager/open (:window-manager context) key {:frame false
                                                                             :resizable true})
  (when maximized
    (reg.framework.electron.window-manager/maximize (:window-manager context) key))))


(defmethod reg.framework.reconciler.reconcile/reconcile-remove :window [context previous next]
  (reg.framework.electron.window-manager/close (:window-manager context) (:key (second previous))))


(defmethod reg.framework.reconciler.reconcile/reconcile-update :window [context previous next]
  (let [prev-opts (second previous)
        next-opts (second next)]

    ;; maximize or unmaximize
    (condp = [(:maximized prev-opts) (:maximized next-opts)]
      [false true]
        (reg.framework.electron.window-manager/maximize (:window-manager context) (:key next-opts))
      [true false]
        (reg.framework.electron.window-manager/unmaximize (:window-manager context) (:key next-opts))
      nil)))

(defmethod reg.framework.reconciler.reconcile/extended-context :window [context previous next]
  (merge context {:window-key (:key (second (or next previous)))}))
