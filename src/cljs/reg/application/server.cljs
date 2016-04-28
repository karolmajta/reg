(ns reg.application.server
  (:require [cljs.reader :refer [read-string]]
            [cljs.core.async :refer [take!]]
            [datascript.core :as d]

            [reg.framework.server.core]

            [reg.application.screens]
            [reg.application.http])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(declare -main)
(set! *main-cli-fn* #(apply -main %&))

(def conn (d/create-conn {}))

(defn handler [e]
  (when (= (:event e) :app/ready)
    (let [[app] (first (d/q '[:find ?a
                              :where [?a :app/id _]] @conn))]
      (d/transact! conn [[:db.fn/retractAttribute app :app/ready?]
                         [:db/add app :app/ready? true]])))

  (when (= (:event e) :app/close)
    (let [[app] (first (d/q '[:find ?a
                              :where [?a :app/id _]] @conn))]
      (d/transact! conn [[:db.fn/retractAttribute app :app/closed?]
                         [:db/add app :app/closed? true]])))

  (when (= (:event e) :app/toggle-maximize)
    (let [[app maximized?] (first (d/q '[:find ?a ?maximized
                                         :where [?a :app/maximized? ?maximized]] @conn))]
      (d/transact! conn [[:db.fn/retractAttribute app :app/maximized?]
                         [:db/add app :app/maximized? (not maximized?)]])))

  (when (= (:event e) :app/login)
    (let [[app] (first (d/q '[:find ?a
                              :where [?a :app/id _]] @conn))]
      (d/transact! conn [[:db/add app :app/session {:pending true}]])
      (take!
        (reg.application.http/login (:event-data e))
        #(if (= :error (:status %))
          (d/transact! conn [[:db.fn/retractAttribute app :app/session]
                             [:db/add app :app/session {:pending false
                                                        :token nil
                                                        :error (get-in % [:data :message])}]])
          (d/transact! conn [[:db.fn/retractAttribute app :app/session]
                             [:db/add app :app/session {:pending false
                                                        :token (get-in % [:data :token])}]])))))

  (when (= (:event e) :app/select-workspace)
    (let [[app] (first (d/q '[:find ?a
                              :where [?a :app/id _]] @conn))]
      (d/transact! conn [[:db.fn/retractAttribute app :app/workspace-focus]
                         [:db.fn/retractAttribute app :app/selected-workspace]
                         [:db/add app :app/selected-workspace (get-in e [:event-data :workspace])]])))

  (when (= (:event e) :projects-dashboard/select-project)
    (let [[app] (first (d/q '[:find ?a
                              :where [?a :app/id]] @conn))]
      (d/transact! conn [[:db.fn/retractAttribute app :app/workspace-focus]
                         [:db/add app :app/workspace-focus (:event-data e)]])))

  (when (= (:event e) :users-dashboard/select-user)
    (let [[app] (first (d/q '[:find ?a
                              :where [?a :app/id]] @conn))]
      (d/transact! conn [[:db.fn/retractAttribute app :app/workspace-focus]
                         [:db/add app :app/workspace-focus (:event-data e)]]))))

(defn app []
  (let [app (into {} (d/entity @conn (first (first (d/q '[:find ?a
                                                          :where [?a :app/id _]] @conn)))))
        projects (map #(into {} (d/entity @conn %)) (d/q '[:find [?p ...]
                                                           :where [?p :data.project/name _]] @conn))
        users (map #(into {} (d/entity @conn %)) (d/q '[:find [?u ...]
                                                        :where [?u :data.user/name _]] @conn))]
    (when-not (:app/closed? app)
      [:app {:on-ready {:event :app/ready}}
       (when (:app/ready? app)
         [:window {:key :app-window
                   :maximized (:app/maximized? app)}
          [:content {:key :app-content}
           (if (:token (:app/session app))
             (condp = (:app/selected-workspace app)
               :projects (reg.application.screens/projects-dashboard projects (:app/workspace-focus app))
               :users (reg.application.screens/users-dashboard users (:app/workspace-focus app)))
             (reg.application.screens/login (:app/session app)))]])])))

(defn -main
  [& args]
  (enable-console-print!)

  (d/transact! conn [{:db/id -1
                      :app/id :application
                      :app/closed? false
                      :app/ready? false
                      :app/maximized? false
                      :app/selected-workspace :projects}
                     {:db/id -2
                      :data.project/id (random-uuid)
                      :data.project/name "Lion King"}
                     {:db/id -3
                      :data.project/id (random-uuid)
                      :data.project/name "Pulp Fiction"}
                     {:db/id -4
                      :data.project/id (random-uuid)
                      :data.project/name "Batman"}
                     {:db/id -5
                      :data.user/id (random-uuid)
                      :data.user/name "Alice"}
                     {:db/id -6
                      :data.user/id (random-uuid)
                      :data.user/name "Bob"}])

  (let [render (reg.framework.server.core/start handler)]
    (d/listen! conn ::data-change-watch #(render (app)))
    (render (app))))
