Purpose
=======
With this library you can access to a HS-485-Bus from ELV via USB-PC-Interface HS485 PCI. 
Newer Linux-Kernels loads automatically the ftdi_sio-Module for access the interface as Serial Port.

Usage
=====

Initialize
----------
The PC needs also a address on the HS-485-Bus for communication. While initializing you have to define the Serial interface and the 
local address on the bus (in this example 3).

    HS485 hs485=new HS485("/dev/ttyUSB0",3)
    

Enumerate Clients
-----------------
You can enumerate all clients on the Bus. Every Member has his own Address (as Integer)

	List<Integer> clients = hs485.listClients()