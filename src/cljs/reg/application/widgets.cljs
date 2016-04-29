(ns reg.application.widgets
  (:require [clojure.string :as s]
            [reagent.core :as r]
            [goog.events :as events]
            [goog.events.EventType :as EventType]))


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

;<ul class="list-group">
;<li class="list-group-item">
;<img class="img-circle media-object pull-left" src="/assets/img/avatar.jpg" width="32" height="32">
;<div class="media-body">
;<strong>List item title</strong>
;<p>Lorem ipsum dolor sit amet.</p>
;</div>
;</li>
;<li class="list-group-item">
;<img class="img-circle media-object pull-left" src="/assets/img/avatar2.png" width="32" height="32">
;<div class="media-body">
;<strong>List item title</strong>
;<p>Lorem ipsum dolor sit amet.</p>
;</div>
;</li>
;...
;</ul>

(defn app-bar [options]
  (let [dragging-delta (r/atom nil)
        listeners (r/atom {EventType/MOUSEDOWN nil
                           EventType/MOUSEUP nil
                           EventType/MOUSEMOVE nil})]
    (r/create-class {
      :component-did-mount
        (fn [this]
          (swap! listeners assoc EventType/MOUSEDOWN
                 (events/listen (.getDOMNode this) EventType/MOUSEDOWN
                    #(reset! dragging-delta [(.-clientX %) (.-clientY %)])))
          (swap! listeners assoc EventType/MOUSEUP
                 (events/listen js/window EventType/MOUSEUP #(reset! dragging-delta nil)))
          (swap! listeners assoc EventType/MOUSEMOVE
                 (events/listen js/window EventType/MOUSEMOVE
                   #(when @dragging-delta
                     (let [new-x (- (.-screenX %) (first @dragging-delta))
                           new-y (- (.-screenY %) (second @dragging-delta))]
                       (.moveTo js/window new-x new-y))))))
      :component-will-unmount
        (fn [this]
          (doseq [[_ event-key] @listeners]
            (events/unlistenByKey event-key)))
      :reagent-render
        (fn app-bar [options]
          (let [{:keys [on-close on-maximize on-workspace-select]
                 :or {on-close #(do) on-maximize #(do) on-workspace-select #(do)}} options]
            [:div.toolbar.toolbar-header
             [:div {:style {:float :left :font-size "20px" :height "33px"}}
              [:a {:href "#" :on-click #(on-close {})
                   :style {:color "red" :line-height "33px" :padding "0 5px"}} [:span.icon.icon-record]]
              [:a {:href "#" :on-click #(on-maximize {})
                   :style {:color "green" :line-height "33px" :padding "0"}} [:span.icon.icon-record]]]
             (when (:show-workspaces options)
               [:div.toolbar-actions {:style {:float :left}}
                [:div.btn-group
                 [:button.btn.btn-default {:on-click #(on-workspace-select {:workspace :projects})
                                           :class (when (= :projects (:selected-workspace options)) "active")} [:span.icon.icon-video]]
                 [:button.btn.btn-default {:on-click #(on-workspace-select {:workspace :users})
                                           :class (when (= :users (:selected-workspace options)) "active")} [:span.icon.icon-users]]]])
             [:h1.title {:style {:height "33px" :line-height "33px"}} (:title options)]]))})))


(defn list [options]
  (let [items (:items options)
        selected (:selected options)
        on-select (:on-select options)]
    (into [:ul.list-group] (for [[item-key item] items] [:li.list-group-item
                                                         {:on-click #(on-select item-key)
                                                          :class (when (= item-key selected) "active")}
                                                         [:div.media-body
                                                          [:strong item]]]))))

(defn details [options]
  [:div
   (if (:item options)
     (str "You're viewing " (:item options) ".")
     "Please pick an item from the list...")])
