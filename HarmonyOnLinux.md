# Problem #

When using a database server, Harmony generates many short TCP connections.
To avoid reaching the maximum number of connection, and have database errors, you should authorize reclycling and reusing of TCP sockets.


# Solutions #

## Temporary Solution ##

```sh

sudo echo "1" > /proc/sys/net/ipv4/tcp_tw_recycle
sudo echo "1" > /proc/sys/net/ipv4/tcp_tw_reuse```


This will be undone at restart.

## Permanent Solution ##

```sh

sudo echo "net.ipv4.tcp_tw_reuse=1" >> /etc/sysctl.conf
sudo echo "net.ipv4.tcp_tw_recycle=1" >> /etc/sysctl.conf```

You need to restart your session to apply these settings.