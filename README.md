# MtoM
Android app to read e-mails and send them as sms.

## Configuration
Move to file `build.gradle` inside app and change the next parameters:

    productFlavors {
        gmail {
            applicationIdSuffix ".gmail"
            // Config
            buildConfigField "String", "HOST", '"pop.gmail.com"'
            buildConfigField "int", "PORT", "995"
            buildConfigField "String", "USER", '"YourUsername@gmail.com"'
            buildConfigField "String", "PASS", '"YourPassword"'
            buildConfigField "String", "PROP_SSL_ENABLED", '"true"'
        }

        nauta {
            applicationIdSuffix ".nauta"
            // Config
            buildConfigField "String", "HOST", '"pop.nauta.cu"'
            buildConfigField "int", "PORT", "110"
            buildConfigField "String", "USER", '"YourUsername@nauta.cu"'
            buildConfigField "String", "PASS", '"YourPassword"'
            buildConfigField "String", "PROP_SSL_ENABLED", '"false"'
        }
    }

or add yor product flavor.:relaxed:

