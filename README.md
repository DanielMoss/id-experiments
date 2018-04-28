# id-experiments
This project experiments with representations of IDs for types, and how to provide JSON encodings for such things. In particular, it
deals with the case where for a type `T`, we know that instances of `T` have a specific number of identifiers.

The example in this project deals with four types of vehicle, each type of which has either one or two identifiers. For example, the
`Car` type has a single identifier, which could be the license plate number. The `Lorry` type has two identifiers, which could be the
pairing of the identifiers for the cab and the trailer.

## Results
### Compilation
The compiler will enforce that:
* For any type `T`, we can only have one kind of `ID` representation (either a `MonoID` or a `DualID`).
* If for a type `T` we are able to create a `MonoID[T]`, then we will be unable to create a `DualID[T]`, and vice versa.

Creation of `ID`s is straightforward and works as described by the above rules:
```
MonoID[Car.type]("id")         // compiles
DualID[Car.type]("id1", "id2") // does not compile
ID[Car.type]("id")             // compiles
ID[Car.type]("id1", "id2")     // does not compile

MonoID[Lorry.type]("id")         // does not compile
DualID[Lorry.type]("id1", "id2") // compiles
ID[Lorry.type]("id")             // does not compile
ID[Lorry.type]("id1", "id2")     // compiles
```

### JSON encoding and decoding
Where `T` is a subtype of `VehicleType`, the following all result in the same JSON:
```
MonoID[T]("id").asJson
(MonoID[T]("id"): ID[T]).asJson
(MonoID[T]("id"): ID[VehicleType]).asJson
```

Similarly, where `json` is some valid JSON for a `MonoID[T]`, the following decodings are all supported losslessly:
```
decode[MonoID[T]](json)
decode[ID[T]](json)
decode[ID[VehicleType]](json)
```

Similar assertions can be made for `DualID`.

## External projects used
* [circe](https://github.com/circe/circe) - For JSON encoding and decoding.
* [Enumeratum](https://github.com/lloydmeta/enumeratum) - For representing the enumeration of vehicle types.
* [Typelevel Scala](https://github.com/typelevel/scala) - For the `ValueOf` type class, which provides a way of summoning the values
of singleton types. This is used to provide a way of summoning the JSON encoding of singleton types without requiring the instance of
the type.
