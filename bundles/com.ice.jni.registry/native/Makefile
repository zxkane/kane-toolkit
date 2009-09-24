JAVA_HOME = "C:/Program Files/Java/jdk1.6.0_14"

CXXFLAGS = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/win32

TARGET = ../os/win32/x86/ICE_JNIRegistry.dll

all:	$(TARGET)

$(TARGET):	registrykey.o
	g++ -shared -Wl,--kill-at -o $(TARGET) $^ -lole32 -luuid

clean:
	rm -f *.o
