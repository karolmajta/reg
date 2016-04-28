(ns reg.application.client
  (:require [reg.framework.client.core]
            [reg.application.widgets]))

(defn -main [& args]
  (enable-console-print!)
  (reg.framework.client.core/start
    {:login-widget reg.application.widgets/login-form
     :list reg.application.widgets/list
     :details reg.application.widgets/details
     :app-bar reg.application.widgets/app-bar}
    (.getElementById js/document "application-container")))
