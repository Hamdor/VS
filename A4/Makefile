CC=g++
CCFLAGS=-std=c++14 -O3
LDFLAGS=-lrt
all:
	$(CC) $(CCFLAGS) $(LDFLAGS) -I include/ -I sniffer/ sniffer/datagram.c src/args.cpp src/main.cpp -o a4_out

clean:
	rm -f a4_out

