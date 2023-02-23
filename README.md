# keycloak-dashbuilder

This is a Quarkus project that connects to Keycloak REST API as a proxy and build Dashboards using the [Dashbuilder](https://www.dashbuilder.org/) project. You can see an example bellow:

![Keycloak-Dashbuilder Home](/img/home.png "Keycloak-Dashbuilder Home")

So, you can now build your own dashboard using Dashbuilder YAML and analyze your data properly.

# Table of Contents
1. [Tested Versions](#tested-versions)
2. [Install and Setup Keycloak](#install-and-setup-keycloak)
3. [Configuring Keycloak-dashbuilder project](#configuring-keycloak-dashbuilder-project)
4. [Other Integrations](#other-integrations)

## Tested Versions

| Keycloak Version 	| RH-SSO Version 	| Quarkus Dashbuilder Version 	|
|------------------	|----------------	|-----------------------------	|
| 20.0.5           	| -              	| 0.26.1                      	|
| 20.0.5           	| -              	| 0.26.1                      	|
| -                	| 7.6.1          	| 0.26.1                      	|

_You need to use [kcadm.sh](#using-kcadm) instead import the Realm File in case of running RH-SSO 7.6._

## Install and Setup Keycloak

### By importing REALM File

1. First you need to [download](https://www.keycloak.org/downloads) and run Keycloak 20.x as following:

```
sh bin/kc.sh start-dev
```

2. Import the [config/realm-export.json](https://raw.githubusercontent.com/pedro-hos/keycloak-dashbuilder/main/config/realm-export.json) on your running Keycloak environment

![Keycloak Create Realm](/img/create_realm.png "Keycloak Create Realm")

3. Create a new User at Keycloak Dashbuilder Realm. On this new user go to **Role Mapping** tab and find by `realm-admin` role and assing it to the new user created. This new user should use this role in order to have admin permissions.

![Keycloak User Config](/img/user_config.png "Keycloak User Config")

4. Go to **Clients** and click on **dashboard** client, then go to **Credentials** and copy the **Secret** value and save.

![Keycloak Secret Config](/img/secret_config.png "Keycloak Secret Config")

### Using kcadm

1. First you need to [download](https://www.keycloak.org/downloads) and run Keycloak 20.x as following:

```
sh bin/kc.sh start-dev
```

2. Login at your realm using `kcadm.sh`. So, go to `bin/` folder and run the following commands:

~~~
./kcadm.sh config credentials --server http://localhost:8080/ --realm master --user {MASTER_USER} --password {MASTER_PASS}
~~~

3. Create new Dashbuilder Realm

```
./kcadm.sh create realms -s realm=dashbuilder -s enabled=true
```

4. Enable events on **Events**, **Config** tab and enable **Save Events** for **Login Events Settings** and **Admin Events Settings**

```
./kcadm.sh update events/config -r dashbuilder -s "eventsEnabled=true" -s "adminEventsEnabled=true"
```
_if you are using metrics, you'd to add the metrics-listener on Events Listeners_

```
./kcadm.sh update events/config -r dashbuilder -s "eventsEnabled=true" -s "adminEventsEnabled=true" -s "eventsListeners+=metrics-listener"
```

5. Create new client on dashbuilder realm:

```
./kcadm.sh create clients -r dashbuilder -s clientId=dashboards -s enabled=true -s clientAuthenticatorType=client-secret -s baseUrl=http://localhost:8081/dashboards -s redirectUris='["http://localhost:8081/dashboards/*"]'
Created new client with id '6c922c7c-6b2d-44ff-909f-33279b5ff257'
```

_Copy the client id to use on the next step_

6. Creating the client secret with the `client id` created on step 5.

```
./kcadm.sh create -r dashbuilder clients/$CID/client-secret
```

7. Retrieve the client secret with the `client id` created on step 5.

```
./kcadm.sh get -r dashbuilder clients/$CID --fields 'secret'
{
  "secret" : "FafS0NayqjSbCDjOTmYvqgK0NUeaoUML"
}
```

_Copy that value and sabe, we'll use it before_

8. Now, we need to create a new user and set the password.

```
./kcadm.sh create users -r dashbuilder -s username=testuser -s enabled=true
./kcadm.sh set-password -r dashbuilder --username testuser --new-password secret
```
_You can change the new user, and password as needed_

9. Finally, we need to grant the `realm-admin` role to the new user to be able to use the REST API

```
./kcadm.sh add-roles -r dashbuilder --uusername testuser --cclientid realm-management --rolename realm-admin
```

## Configuring Keycloak-dashbuilder project

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

You can retrieve the secret by `kcadm.sh` as following, where `$CID` is the created client id:

```
./kcadm.sh get -r dashbuilder clients/$CID --fields 'secret'
{
  "secret" : "FafS0NayqjSbCDjOTmYvqgK0NUeaoUML"
}
```

or going to **Clients** -> **dashboards** -> **Credentials** tab

![Keycloak Secret Config](/img/secret_config.png "Keycloak Secret Config")

2. You can now compile and run the project

```
./mvnw quarkus:dev
```

3. Finally you can access the **Dashboards**, `http://localhost:8081/dashboards/`. You should be redirected to the Keycloak Login Page, so, login with the created user.

## Other Integrations

You can also, compile and install [
keycloak-metrics-spi
](https://github.com/aerogear/keycloak-metrics-spi) and [keycloak-health-checks](https://github.com/thomasdarimont/keycloak-health-checks) as your SPI and use it on  Dashbuilders
