(ns reg.framework.reconciler.content
  (:require [reg.framework.electron.window-manager]
            [reg.framework.reconciler.reconcile]
            [reg.framework.reconciler.utils :refer [bind-events-on-add
                                                    unbind-events-on-remove]]))

(defn hiccup-content [element]
  (or (get element 2) [:div.no-content]))

(defmethod reg.framework.reconciler.reconcile/reconcile-add :content [context previous next]
  (reg.framework.electron.window-manager/send-hiccup
    (:window-manager context)
    (:window-key context)
    (hiccup-content next)))


(defmethod reg.framework.reconciler.reconcile/reconcile-remove :content [context previous next]
  (reg.framework.electron.window-manager/send-hiccup
    (:window-manager context)
    (:window-key context)
    (hiccup-content nil)))


(defmethod reg.framework.reconciler.reconcile/reconcile-update :content [context previous next]
  (reg.framework.electron.window-manager/send-hiccup
    (:window-manager context)
    (:window-key context)
    (hiccup-content next)))

(defmethod reg.framework.reconciler.reconcile/should-reconcile-children :content [context previous next] false)