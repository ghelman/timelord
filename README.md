# Timelord

A simple client application to capture time tracking information on your local system.

# Overview

The basic idea is that everyone hates time tracking. The moment you start working in a
job that has a lot of randomization and a lot of time tracking, nearly all the major
tools out there are too burdensome to use. If you are not tracking your time every
15-30 minutes, then you've already forgot what you were doing.

So Timelord is a simple application for people who sit in front of their computer most
of the time. It continually pesters you to tell it what you have been doing so that
you don't let the time get away from. Every 15m it will popup and ask what you are
doing. Once you save it, it will go away for 15m. That's it.

At the end of the week, export your data in Excel and upload it or copy-paste it into
your corporate time tracking solution.

# Build

The project is build using Maven:
`mvn package`

The OSX Bundle is built using the Oracle Bundler Ant Task:
`ant bundle`

# Downloads

The latest release is available in releases:
* v2.71 for OSX - https://github.com/chanomie/timelord/releases/download/2.71/Timelord-2.71.dmg
* v.25 for Windows - https://github.com/chanomie/timelord/releases/download/2.71/Timelord-2.5.zip