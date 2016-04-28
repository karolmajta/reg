(ns reg.application.widgets
  (:require [clojure.string :as s]
            [reagent.core :as r]))


(defn login-form [options]
  (let [credentials (r/atom {:email "" :password ""})
        validation-error (r/atom nil)
        on-click #(if (or (s/blank? (:email @credentials)) (s/blank? (:password @credentials)))
                   (reset! validation-error "Both fields are required.")
                   (do
                     (reset! validation-error nil)
                     ((:on-login-attempt options) @credentials)))]
    (fn [options]
      [:div
       [:input {:type :text
                :required true
                :placeholder "Email address"
                :value (:email @credentials)
                :on-change #(swap! credentials assoc :email (-> % .-target .-value))}]
       [:input {:type :password
                :required true
                :placeholder "Password"
                :value (:password @credentials)
                :on-change #(swap! credentials assoc :password (-> % .-target .-value))}]
       (if-not (:login-pending options)
         [:button.btn--raised {:on-click on-click} "click to log in"]
         [:div.color--green "Attempting login"])
       [:div.color--red @validation-error]
       [:div.color--red (:login-failed options)]])))


(defn app-bar [options]
  (let [{:keys [on-close on-maximize on-workspace-select]
         :or {on-close #(do) on-maximize #(do) on-workspace-select #(do)}} options]
    [:div
     [:a {:href "#" :on-click #(on-close {})} "[x]"]
     [:a {:href "#" :on-click #(on-maximize {})} "[*]"]
     (when (:show-workspaces options)
       [:span
        " | "
        [:a {:href "#" :on-click #(on-workspace-select {:workspace :projects})} "projects"]
        " | "
        [:a {:href "#" :on-click #(on-workspace-select {:workspace :users})} "users"]])]))


(defn list [options]
  (let [items (:items options)
        on-select (:on-select options)]
    (into [:div] (for [[item-key item] items] [:div
                                                [:a {:href "#"
                                                     :on-click #(on-select item-key)} item]]))))

(defn details [options]
  [:div
   (if (:item options)
     (str "You're viewing " (:item options) ".")
     "Please pick an item from the list...")])
