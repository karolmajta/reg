(ns reg.application.screens)


(defn login [session]
  [:div
    ['app-bar {:on-close {:event :app/close}
               :on-maximize {:event :app/toggle-maximize}
               :show-workspaces false}]
    [:div.g--10.m--1.card
     ['login-widget {:login-pending (:pending session)
                     :login-failed (:error session)
                     :on-login-attempt {:event :app/login}}]]])

(defn projects-dashboard [projects selected-project]
  (let [items (into {} (map #(vector % (:data.project/name %)) projects))]
    [:div
      ['app-bar {:on-close {:event :app/close}
                 :on-maximize {:event :app/toggle-maximize}
                 :show-workspaces true
                 :on-workspace-select {:event :app/select-workspace}}]
      [:div.g--10.m--1.container--start
       [:div.g--5.card
        ['list {:items items :on-select {:event :projects-dashboard/select-project}}]]
       [:div.g--6.m--1.card
         ['details {:item (:data.project/name selected-project)}]]]]))

(defn users-dashboard [users selected-user]
  (let [items (into {} (map #(vector % (:data.user/name %)) users))]
    [:div
     ['app-bar {:on-close {:event :app/close}
                :on-maximize {:event :app/toggle-maximize}
                :show-workspaces true
                :on-workspace-select {:event :app/select-workspace}}]
     [:div.g--10.m--1.container--start
      [:div.g--5.card
       ['list {:items items :on-select {:event :users-dashboard/select-user}}]]
      [:div.g--6.m--1.card
       ['details {:item (:data.user/name selected-user)}]]]]))
