[![Build Status](https://travis-ci.org/GDATASoftwareAG/Graylog-Plugin-AlertManager-Callback.svg?branch=master)](https://travis-ci.org/GDATASoftwareAG/Graylog-Plugin-AlertManager-Callback)
&nbsp;&nbsp;&nbsp;[![Codacy Badge](https://api.codacy.com/project/badge/Grade/c3a48bd0e2e64a2499cc25c7d2a3abe6)](https://app.codacy.com/app/StefanHufschmidt/Graylog-Plugin-AlertManager-Callback?utm_source=github.com&utm_medium=referral&utm_content=GDATASoftwareAG/Graylog-Plugin-AlertManager-Callback&utm_campaign=Badge_Grade_Dashboard)
&nbsp;&nbsp;&nbsp;[![codecov](https://codecov.io/gh/GDATASoftwareAG/Graylog-Plugin-AlertManager-Callback/branch/master/graph/badge.svg)](https://codecov.io/gh/GDATASoftwareAG/Graylog-Plugin-AlertManager-Callback)
&nbsp;&nbsp;&nbsp;[![Known Vulnerabilities](https://snyk.io/test/github/GDATASoftwareAG/Graylog-Plugin-AlertManager-Callback/badge.svg)](https://snyk.io/test/github/GDATASoftwareAG/Graylog-Plugin-AlertManager-Callback)

# Graylog AlertManager Notification Plugin   
This plugin can be used for connecting [Graylog](https://www.graylog.org/) alerts to the [Prometheus](https://prometheus.io/) [AlertManager](https://prometheus.io/docs/alerting/alertmanager/).

The plugin development is based on [Graylog2/graylog-plugin-sample](https://github.com/Graylog2/graylog-plugin-sample) which is mentioned in the [Graylog plugin documentation](http://docs.graylog.org/en/2.4/pages/plugins.html).

## Use Case
You are using a Graylog for checking the logs for errors, a Prometheus for checking the service metrics and you would like to organize your alerts with a AlertManager you need to get your Graylog alerts into the AlertManager.
This plugin provides the possibility to send your Graylog notifications with a AlertManager-Callback to your AlertManager.

## AlertManager Endpoint
The plugin uses the `/api/v1/alerts` endpoint of AlertManager. You can find some documentation about this endpoint [here](https://prometheus.io/docs/alerting/clients/).

## Provided Information
The plugin provides the AlertManager several information out of the box:
* `stream_title` - The title of the stream triggering the alert condition in Graylog
* `triggered_at` - The time of triggering the alert condition in Graylog
* `triggered_rule_description` - The generated rule description of triggered alert condition in Graylog
* `triggered_rule_title` - The title of alert condition rule in Graylog

All of those information will be added as annotation.

The values `startsAt`, `endsAt` and `generatorURL` will be transmitted to the AlertManager as well.
`startsAt` will be set to the point of time when the condition triggered the alert.
`endsAt` will be set to the point of time when the condition triggered the alert plus the set grace time which is configured for the alert.

Additionally you can configure your own custom annotations and labels which should be submitted to the AlertManager (see screenshot below).
You can use the [JMTE Template](https://cdn.rawgit.com/DJCordhose/jmte/master/doc/index.html) as you might already know from the [Graylog E-Mail Notification Callback](http://docs.graylog.org/en/2.5/pages/streams/alerts.html#email-alert-notification).

List of provided keys you can use inside JMTE Templates:
* `stream_url` - The stream url.
* `stream` - The specific stream object. There you can use the properties of the stream object e.g. `stream.title`
* `alertCondition` - The specific triggered alert condition. There you can use the properties of the alert condition oject e.g. `alertCondition.createdAt`
* `check_result` - The specific check result. There you can use the properties of the check result object e.g. `check_result.triggeredAt`
* `backlog` - A list containing messages matching the triggered condition if any. You can iterate through them with `${foreach backlog message}${message} ${end}`
* `backlog_size` - The amount of matching messages.

## How to deploy on Graylog
You can easily build the plugin by executing `./gradlew build -x check --no-daemon`. 
Afterwards there should be a `.jar` file inside the `build/libs/` directory.
Follow the instructions mentioned [here](http://docs.graylog.org/en/2.4/pages/plugins.html#installing-and-loading-plugins) to deploy this `.jar` file.

## Screenshots
![Configuration of Callback](images/New_AlertManager_Callback_Window.png)

## Planned Features
You would like to contribute anything? - Take a look at [CONTRIBUTING.md](CONTRIBUTING.md).

## Known Issues
* The test alert will not be shown in AlertManager
    * When clicking on `Test` to test your callback configuration it will show a green ok bar when everything is fine but the alert will not be shown in the AlertManager
    * You have to create a stream with a alert condition to test the callback alert in AlertManager

You would like to contribute anything? - Take a look at [CONTRIBUTING.md](CONTRIBUTING.md).

## License
See [LICENSE](LICENSE)

## Copyright

Copyright (c) 2019 G DATA CyberDefense AG and other authors.
