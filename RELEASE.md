# How to release

If it is the first time you release with sbt-pgp 2.x import the keypairs, see [https://github.com/sbt/sbt-pgp#importing-key-pair](https://github.com/sbt/sbt-pgp#importing-key-pair).

Run `GPG_TTY=$(tty) sbt +publishSigned`

Run `sbt sonatypeRelease`

