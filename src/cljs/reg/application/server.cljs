(ns reg.application.server
  (:require [cljs.reader :refer [read-string]]
            [cljs.core.async :refer [take!]]

            [reg.framework.server.core]

            [reg.application.screens]
            [reg.application.http])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(declare -main)
(set! *main-cli-fn* #(apply -main %&))

(def state (atom {:app-ready? false
                  :app-closed? false
                  :session nil
                  :users ["A" "B" "C"]
                  :shown-user-windows #{}}))

(defn handler [e]
  (when (= (:event e) :app/ready)
    (swap! state assoc :app-ready? true))
  (when (= (:event e) :app/close)
    (swap! state assoc :app-closed? true))
  (when (= (:event e) :app/login)
    (swap! state assoc :session {:pending :true
                                 :token nil
                                 :email (get-in e [:event-data :email])})
    (take! (reg.application.http/login (:event-data e)) #(if (= :error (:status %))
                                                          (swap! state assoc :session {:pending false
                                                                                       :token nil
                                                                                       :error (get-in % [:data :message])})
                                                          (swap! state assoc :session {:pending false
                                                                                       :token (get-in % [:data :token])}))))
  (when (= (:event e) :app/user-select)
    (swap! state assoc :focused-user (:event-data e))))


(defn app []
  (when-not (:app-closed? @state)
    [:app {:on-ready {:event :app/ready}}
     (when (:app-ready? @state)
         [:window {:key :app-window
                   :on-close {:event :app/close
                              :prevent-default true}}
          [:content {:key :app-content}
           (if-not (get-in @state [:session :token])
            (reg.application.screens/login (:session @state))
            (reg.application.screens/dashboard (:users @state) (:focused-user @state)))]])]))

(defn -main
  [& args]
  (enable-console-print!)
  (let [render (reg.framework.server.core/start handler)]
    (add-watch state ::data-change-watch #(render (app)))
    (render (app))))
