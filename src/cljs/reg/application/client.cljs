(ns reg.application.client
  (:require [reg.framework.client.core]
            [reagent.core :as r]))

(defn dropdown []
  (let [open? (r/atom false)]
    (fn []
      [:div
       [:button {:on-click #(swap! open? not)} "click to toggle open/closed"]
       (when @open?
         [:ul
          [:li "Item one"]
          [:li "Item two"]
          [:li "Item three"]])])))

(defn -main [& args]
  (enable-console-print!)
  (reg.framework.client.core/start
    {:dropdown dropdown}
    (.getElementById js/document "application-container")))