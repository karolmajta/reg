(ns reg.application.screens)


(defn login [session]
  [:div.window
    ['app-bar {:title "Login"
               :on-close {:event :app/close}
               :on-maximize {:event :app/toggle-maximize}
               :show-workspaces false}]
    [:div.window-content
     ['login-widget {:login-pending (:pending session)
                     :login-failed (:error session)
                     :on-login-attempt {:event :app/login}}]]])

(defn projects-dashboard [projects selected-project]
  (let [items (into {} (map #(vector % (:data.project/name %)) projects))]
    [:div.window
      ['app-bar {:title "Projects dashboard"
                 :on-close {:event :app/close}
                 :on-maximize {:event :app/toggle-maximize}
                 :show-workspaces true
                 :on-workspace-select {:event :app/select-workspace}
                 :selected-workspace :projects}]
      [:div.window-content
       [:div.pane-group
         [:div.pane.sidebar
          ['list {:items items
                  :selected selected-project
                  :on-select {:event :projects-dashboard/select-project}}]]
         [:div.pane
           ['details {:item (:data.project/name selected-project)}]]]]]))

(defn users-dashboard [users selected-user]
  (let [items (into {} (map #(vector % (:data.user/name %)) users))]
    [:div.window
     ['app-bar {:title "Users workspace"
                :on-close {:event :app/close}
                :on-maximize {:event :app/toggle-maximize}
                :show-workspaces true
                :on-workspace-select {:event :app/select-workspace}
                :selected-workspace :users}]
     [:div.window-content
      [:div.pane-group
        [:div.pane.sidebar
         ['list {:items items
                 :selected selected-user
                 :on-select {:event :users-dashboard/select-user}}]]
        [:div.pane
         ['details {:item (:data.user/name selected-user)}]]]]]))
