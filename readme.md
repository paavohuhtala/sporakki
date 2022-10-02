# sporakki

HSL timetables for Wear OS

## Core technologies

- Kotlin
- Jetpack Compose
- Apollo Kotlin
- [Digitransit Stops API](https://digitransit.fi/en/developers/apis/1-routing-api/stops/)

## How to update GrahpQL schema

```bash
./gradlew :app:downloadApolloSchema --endpoint='https://api.digitransit.fi/routing/v1/routers/hsl/index/graphql' --schema=app/src/main/graphql/me/paavo/sporakki/schema.graphqls
```

## License

See [license.md](./license.md).
