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
      [:div.card.g--10.m--1
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


(defn list [options]
  (let [items (:items options)
        on-select (:on-select options)]
    (into [:div.card.g--2.m--1] (for [item items] [:div
                                                    [:a {:href "#"
                                                         :on-click #(on-select item)} item]]))))

(defn details [options]
  [:div.card.g--6.m--1
   (if (:item options)
     (str "You're viewing " (:item options) ".")
     "Please pick an item from the list...")])
