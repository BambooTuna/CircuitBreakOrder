# CircuitBreakOrder

## 概要
- BitFlyerのサーキットブレイク価格に売りと買い指値（有効期限が１分）を毎分入れる
- 新規注文を出して90秒後に約定確認を行い、約定していたらDiscordにて通知を送る
- 本体の起動時、終了時に通知を送る  

証拠金不足にならない限り、普通に裁量トレードをしても、間違えてキャンセルしても、問題ないです。

## 対象者
- BitflyerのApiKeyが取得できる
- DiscordのWebHookのURLを取得できる
- ターミナルの使い方がわかる
- dockerの使い方がわかる(インストールしてある)


## ApiKey設定
./src/main/resources/apiKey.confファイルにBitflyerとDiscordのApiKeyを書く  

- **Bitflyer**  
ApiKeyを取得して以下のように記述する

```conf
  bitflyer_key = "bitflyer_key"
  bitflyer_key = ${?BITFLYER_API_KEY} //変更しない

  bitflyer_secret ="bitflyer_secret"
  bitflyer_secret = ${?BITFLYER_API_SECRET} //変更しない
```

- **Discord**  
[こちら](https://support.discordapp.com/hc/ja/articles/228383668-%E3%82%BF%E3%82%A4%E3%83%88%E3%83%AB-Webhooks%E3%81%B8%E3%81%AE%E5%BA%8F%E7%AB%A0)
を参考にして以下のようなURLを取得する  
https://discordapp.com/api/webhooks/000000000000000000/aaaaaaaaaaaaaaaaaaaaaaaaa  
取得できたら以下のように、KeyとSecretに分けて貼り付ける

```conf
  discord_key = "000000000000000000"
  discord_key = ${?DISCORD_API_KEY} //変更しない

  discord_secret ="aaaaaaaaaaaaaaaaaaaaaaaaa"
  discord_secret = ${?DISCORD_API_SECRET} //変更しない
```
## 枚数設定  
./src/main/resources/apiKey.confファイルを変更してください
※文字列である必要があります！`"〜"`で囲うことを忘れずに  

```
size = "0.01"
```

## 指値価格位置設定 
以下の例だとサーキットブレイク価格の内側1000の位置に指値を巻きます  
CB価格が80万〜120万の場合→80万1000円に買、119万9000円に売  
※こちらは整数数字で入力
```sbtshell
price_delta = 1000
```

## 実際に動かす
```
$ docker build . -t circuit-break-order
```
終わったら
```
$ docker run circuit-break-order
```