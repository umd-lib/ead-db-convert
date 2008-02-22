#!/usr/local/bin/perl -p

tr/[A-Z]/[a-z]/;
s/\[//g;
s/\]//g;
s/(january|february|march|april|may|june|july|august|september|october|november|december)/MMM/g;
s/(winter|spring|summer|fall|autumn)/SSS/g;
s/[0-9]/#/g;
