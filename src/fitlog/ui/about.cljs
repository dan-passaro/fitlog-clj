;; SPDX-FileCopyrightText: 2026 Dan Passaro
;; SPDX-License-Identifier: AGPL-3.0-or-later
(ns fitlog.ui.about)

(defn about-modal []
  [:dialog {:id :about-modal
            :class :modal}
   [:div {:class "modal-box text-left space-y-4"}
    [:h3 {:class "text-lg font-bold"} "About Fitpad"]
    [:p "Fitpad - a workout logging app"]
    [:p "Copyright © 2026 Dan Passaro <dan@danpassaro.dev>"]
    [:p "This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version."]
    [:p "This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details."]
    [:p
     [:a {:class "link link-primary"
          :href "https://www.gnu.org/licenses/agpl-3.0.en.html"
          :target "_blank"}
      "View license"]
     " · "
     [:a {:class "link link-primary"
          :href "https://codeberg.org/dan-passaro/fitpad"
          :target "_blank"}
      "View source code"]]]])
