(ns reg.application.server
  (:require [cljs.reader :refer [read-string]]
            [cljs.core.async :refer [<!]]

            [reg.framework.server.core])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(declare -main)
(set! *main-cli-fn* #(apply -main %&))

(def state (atom {:app-ready? false
                  :app-closed? false
                  :close-click-count 0}))

(defn handler [e]
  (when (= (:event e) :app/ready)
    (swap! state assoc :app-ready? true))
  (when (= (:event e) :app/close)
    (swap! state assoc :app-closed? true))

  (when (= (:event e) :window/close)
    (when (= (:key e) :otherwindow)
      (swap! state assoc :app-closed? true))
    (when (= (:key e) :mywindow)
      (swap! state update :close-click-count inc)))

  (when (= (:event e) :window/close-immediately)
    (swap! state update :close-click-count #(if (< % 10) 10 %))))


(defn app []
    (when-not (:app-closed? @state)
      [:app {:on-ready {:event :app/ready}}
       (when (and (:app-ready? @state) (< (:close-click-count @state) 10))
         [:window {:key :mywindow
                   :on-close {:event :window/close
                              :key :mywindow
                              :prevent-default true}}
          [:content {:key :other-window-content}
           [:div
            [:h1 "You need to click close button 10 times to close this window,
                  but it will not close the application..."]
            [:h3 (str "You have clicked close " (:close-click-count @state) " times")]
            [:hr]
            ['dropdown]
            [:hr]
            [:button {:key :close-button
                      :on-click {:event :app/close}} "You can use this button so close the app too..."]]]])
       (when (:app-ready? @state)
         [:window {:key :otherwindow
                   :on-close {:event :window/close
                              :key :otherwindow
                              :prevent-default true}}
          [:content {:key :otherwindow-content}
           [:div
            [:h1 "Closing this window will cause shutting down the whole app."]
            [:hr]
            (if (< (:close-click-count @state) 10)
              [:button {:key :mywindow-close-button
                        :on-click {:event :window/close-immediately
                                   :key :mywindow}}
               "You can use this button to close the other window immediately"]
              [:div "Looks like you have already closed the other window"])]]])]))

(defn -main
  [& args]
  (enable-console-print!)
  (let [render (reg.framework.server.core/start handler)]
    (add-watch state ::data-change-watch #(render (app)))
    (render (app))))