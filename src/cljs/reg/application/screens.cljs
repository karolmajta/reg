(ns reg.application.screens)


(defn login [session]
  [:div.g--12
   ['login-widget {:login-pending (:pending session)
                   :login-failed (:error session)
                   :on-login-attempt {:event :app/login}}]])

(defn dashboard [users selected-user]
  [:div.g--12.container--start
   ['list {:items users :on-select {:event :app/user-select}}]
   ['details {:item selected-user}]])
