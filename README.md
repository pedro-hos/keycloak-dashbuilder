# keycloak-dashbuilder

This is a Quarkus project that connects to Keycloak REST API as a proxy and build Dashboards using the [Dashbuilder](https://www.dashbuilder.org/) project. You can see an example bellow:

![Keycloak-Dashbuilder Home](/img/home.png "Keycloak-Dashbuilder Home")

So, you can now build your own dashboard using Dashbuilder YAML and analyze your data properly.

## Tested Versions

| Keycloak Version 	| RH-SSO Version 	| Quarkus Dashbuilder Version 	|
|------------------	|----------------	|-----------------------------	|
| 20.0.5           	| -              	| 0.26.1                      	|
| 20.0.5           	| -              	| 0.26.1                      	|
| -                	| 7.6.1          	| 0.26.1                      	|

_You need to make some changes to use RH-SSO instead of Keycloak_

## Install and Setup
### Configuring Keycloak

#### By importing REALM File

1. First you have to [download](https://www.keycloak.org/downloads) and run Keycloak

```
sh bin/kc.sh start-dev
```

2. Import the [confit/realm-export.json](https://raw.githubusercontent.com/pedro-hos/keycloak-dashbuilder/main/config/realm-export.json) on your running Keycloak environment

![Keycloak Create Realm](/img/create_realm.png "Keycloak Create Realm")

3. Create a new User at Keycloak Dashbuilder Realm. 

Go to **Role Mapping** tab and find by `realm-admin` role and assing it to user

![Keycloak User Config](/img/user_config.png "Keycloak User Config")

4. Go to **Clients** and click on **dashboard** client, then go to **Credentials** and copy the Secret value and save.

![Keycloak Secret Config](/img/secret_config.png "Keycloak Secret Config")

#### Using kcadm

x. Enable events on **Events**, **Config** tab and enable **Save Events** for **Login Events Settings** and **Admin Events Settings**

### Configuring Keycloak-dashbuilder project

1. on `src/resources/application.properties` edit the following paramenters:

```
quarkus.http.port=8081
quarkus.http.cors=true
api.keycloak.base-url=[A]

quarkus.oidc.auth-server-url=[B]
quarkus.oidc.client-id=dashboards
quarkus.oidc.application-type=web-app
quarkus.http.auth.permission.authenticated.paths=/*
quarkus.http.auth.permission.authenticated.policy=authenticated

quarkus.http.auth.permission.public.paths=/q/dev
quarkus.http.auth.permission.public.policy=permit

quarkus.oidc.credentials.secret=[C]
```
- [A] Your REST API base URL. For example: `http://localhost:8080/admin/realms/dashbuilder/`
- [B] Your Keycloak REALM URL. For exaple `http://localhost:8080/realms/dashbuilder`
- [C] Secrect that you have copied before. For example `oAQad2nZJRZNDHxC1j59LGpQrlYRBodn`

2. You can now compile and run the project

```
./mvnw quarkus:dev
```

3. Finally you can access the **Dashboards**, `http://localhost:8081/dashboards/`. You should be redirected to the Keycloak Login Page, so, login with the created user.

### Extras

You can also, compile and install [
keycloak-metrics-spi
](https://github.com/aerogear/keycloak-metrics-spi) and [keycloak-health-checks](https://github.com/thomasdarimont/keycloak-health-checks) as your SPI and use it with the Dashbuilders
