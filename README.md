# CircuitBreakOrder

## Setting
- BitflyerのApiKeyを取得して
./src/main/resources/apiKey.conf
```conf
  bitflyer_key = "bitflyer_key"
  bitflyer_key = ${?BITFLYER_API_KEY} //変更しない

  bitflyer_secret ="bitflyer_secret"
  bitflyer_secret = ${?BITFLYER_API_SECRET} //変更しない
```

- [こちら](https://support.discordapp.com/hc/ja/articles/228383668-%E3%82%BF%E3%82%A4%E3%83%88%E3%83%AB-Webhooks%E3%81%B8%E3%81%AE%E5%BA%8F%E7%AB%A0)
を参考にして以下のようなURLを取得する  
https://discordapp.com/api/webhooks/000000000000000000/aaaaaaaaaaaaaaaaaaaaaaaaa

- 取得できたら以下のように、KeyとSecretに分けて貼り付ける

```conf
  discord_key = "000000000000000000"
  discord_key = ${?DISCORD_API_KEY} //変更しない

  discord_secret ="aaaaaaaaaaaaaaaaaaaaaaaaa"
  discord_secret = ${?DISCORD_API_SECRET} //変更しない
```

## Let's Use
```
$ docker build . -t circuit-break-order
```

```
$ docker run circuit-break-order
```