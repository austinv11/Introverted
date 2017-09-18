# Introverted network protocol v1
The Introverted network protocol is designed to be easily extensible.This allows for 
implementations to easily implement arbitrary functionality. This is useful if one wants to
add support for a non JVM language but still wants to add additional functionality without
breaking support for standard clients.

## General network format
All packets are prefixed with the current version number (1 byte). This allows for data 
compatibility verification. Next is an opcode (1 byte), this can either be a cross platform
reserved protocol op or it can be a platform specific opcode. After the op code is the 
payload which is dependent on the op type.

## Network flow
A client can search for running servers by sending a `DISCOVERY` op to all ports and waiting
for a `DISCOVERY_CONFIRM` op in return to signal that there is a running server in the 
specified port. `DISCOVERY_CONFIRM` should contain a string payload representing some
metadata about the server. It should be in the format `{Platform identifier}/{Protocol version}`.
Once a client decides to connect to the server, it should send a `HANDSHAKE` op.The server
can then either refuse the connection attempt via `HANDSHAKE_REFUSE` (along with a reason
string) or it can accept the connection via `HANDSHAKE_CONFIRM`. From here any platform
dependent ops can be exchanged. There is an expected contract that both the client and
server should be able to handle `PING` ops which can be sent from any side to the other
at any time. If a `PING` op is received, the receiving side is expected to send a `PONG`
response as soon as possible. The `PING` and responding `PONG` should both contain a ulong
which is a random, unique identifier. This is used to track specific `PING`/`PONG` payloads
as the `PONG` response should contain the same payload as the `PING` it is responding to. 
Finally, either end can cleanly terminate the connection at any point via 
`CONNECTION_KILLED`. This should contain an integer payload representing an arbitrary exit
code, by convention a `0` exit code is normal and a non-zero exit code is abnormal.

## Reserved protocol ops
These are ops which are platform agnostic and guaranteed to always be implemented.

|op code|operation|payload_type|sending side|
|-------|---------|------------|------------|
|0|DISCOVERY|none|client|
|1|DISCOVERY_CONFIRM|str|server|
|2|HANDSHAKE|none|client|
|3|HANDSHAKE_CONFIRM|none|server|
|4|HANDSHAKE_REFUSE|str|server|
|5|PING|ulong|client/server|
|6|PONG|ulong|client/server|
|7|CONNECTION_KILLED|int|client/server|

## Reserved data type formats
These are data types which are platform agnostic and guaranteed to always be implemented.
All data types are *big endian* and are prefixed with a single byte denoting the data type.

###Boolean
The boolean is data type `0`, it is then followed by a single flag byte in the range 0-1.
When the flag is `0`, the data is represented as `false` and when the flag is `1`, the 
data is represented as `true`. 

###UInt (unsigned int)
The uint is data type `1`, it is then followed by 4 bytes representing the data.

###Int (signed int)
The int is data type `2`, it is then followed by 4 bytes representing the data.

###ULong (unsigned long)
The ulong is data type `3`, it is then followed by 8 bytes representing the data.

###Long (signed long)
The long is data type `4`, it is then followed by 8 bytes representing the data.

###Char
The char is data type `5`, it is then followed by 4 bytes representing the data.

###Decimal
The decimal is data type `6`, it is then followed by bytes which correspond to the 
IEEE 754 floating-point "double format".

###Array
The array is data type `7`, it is then followed by 4 bytes representing the amount of
elements contained, then an indefinite amount of bytes are reserved for reading the array
elements until the number of elements reported is reached.

###Str (string)
The string is data type `8`. It acts like a char array, except the character data type is
implied so the next 4 bytes represents the amount of bytes for characters, followed by 
the actual byte data for the UTF-8 encoding.

###Nil
The nil is data type `9`. It has no other data, this represents an existant value that is
simply empty.

###Map
The map is data type `10`, it is then followed by 4 bytes representing its length, from 
there it must read 2 * map length to get all the values where all evenly indexed objects
(including 0) is a key and the next odd indexed object is the corresponding value.

##JVM Platform
The JVM is the platform which is the primary target of the introverted library, and as
such it has many JVM-specific ops.

###JVM platform implementations
The JVM server metadata should be in the form `JVM-{light/heavy}/{Protocol version}`.

The platform `JVM-light` represents the server implementation written fully in Java 
which contains a limited selection of available ops.

The platform `JVM-heavy` represents the server implementation which utilizes native 
code which contains the full selection of available ops.

