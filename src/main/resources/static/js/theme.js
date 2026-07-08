(function () {
    'use strict';

    var THEME_KEY = 'ams-theme';
    var SCHEME_KEY = 'ams-scheme';

    function applyStoredPreferences() {
        var theme = localStorage.getItem(THEME_KEY) || 'light';
        var scheme = localStorage.getItem(SCHEME_KEY) || 'azure';
        document.documentElement.setAttribute('data-theme', theme);
        document.documentElement.setAttribute('data-scheme', scheme);
    }

    applyStoredPreferences();

    document.addEventListener('DOMContentLoaded', function () {

        var lightBtn = document.getElementById('themeLightBtn');
        var darkBtn = document.getElementById('themeDarkBtn');
        var schemeSelect = document.getElementById('schemeSelect');

        function refreshControls() {
            var theme = document.documentElement.getAttribute('data-theme');
            var scheme = document.documentElement.getAttribute('data-scheme');

            if (lightBtn && darkBtn) {
                lightBtn.classList.toggle('active', theme === 'light');
                darkBtn.classList.toggle('active', theme === 'dark');
            }
            if (schemeSelect) {
                schemeSelect.value = scheme;
            }
        }

        if (lightBtn) {
            lightBtn.addEventListener('click', function () {
                document.documentElement.setAttribute('data-theme', 'light');
                localStorage.setItem(THEME_KEY, 'light');
                refreshControls();
            });
        }

        if (darkBtn) {
            darkBtn.addEventListener('click', function () {
                document.documentElement.setAttribute('data-theme', 'dark');
                localStorage.setItem(THEME_KEY, 'dark');
                refreshControls();
            });
        }

        if (schemeSelect) {
            schemeSelect.addEventListener('change', function () {
                document.documentElement.setAttribute('data-scheme', schemeSelect.value);
                localStorage.setItem(SCHEME_KEY, schemeSelect.value);
            });
        }

        refreshControls();

        document.querySelectorAll('form[data-confirm]').forEach(function (form) {
            form.addEventListener('submit', function (e) {
                if (!window.confirm(form.getAttribute('data-confirm'))) {
                    e.preventDefault();
                }
            });
        });
    });
})();
