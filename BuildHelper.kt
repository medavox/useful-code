package com.elucid_mhealth.elucid

import java.io.File
import java.io.IOException
import java.net.InetAddress

import java.net.NetworkInterface
import java.net.UnknownHostException


/**Place this file in `<project root>/buildSrc`. 
You can use it to provide the IP address of the compiling machine 
to the compiled app (for debugging purposes),
by calling it from your `build.gradle` file with the following groovy syntax:
```groovy
task storeCompileTimeIpAddress {
    //the groovy syntax to access a Kotlin companion object's function is:
    //ClassName.@companion.functionName()
    new BuildHelper().generateCompileTimeLocalIpAddress(generatedSrcDir)
}

preBuild.dependsOn storeCompileTimeIpAddress
```*/
class BuildHelper {
    /**
     * Returns an `InetAddress` object encapsulating what is most likely the machine's LAN IP address.
     *
     * This method is intended for use as a replacement of JDK method [InetAddress.getLocalHost],
     * because that method is ambiguous on Linux systems.
     * Linux systems enumerate the loopback network interface the same way as
     * regular LAN network interfaces,
     * but the JDK `InetAddress.getLocalHost` method does not specify the algorithm used to
     * select the address returned under such circumstances,
     * and will often return the loopback address, which is not valid for network communication.
     * Details [here](http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4665037).
     *
     *
     * This method will scan all IP addresses on all network interfaces on the host machine,
     * to determine the IP address most likely to be the machine's LAN address.
     *
     * If the machine has multiple IP addresses,
     * this method prefers a site-local IP address (e.g. 192.168.x.x or 10.10.x.x, usually IPv4).
     * If the machine has one or more, it will return the first site-local address.
     * If the machine does not hold a site-local address,
     * this method will return simply the first non-loopback address found (IPv4 or IPv6).
     *
     *
     * If this method cannot find a non-loopback address using this selection algorithm,
     * it will fall back to returning the result of JDK method `InetAddress.getLocalHost`.
     *
     * @throws UnknownHostException If the LAN address of the machine cannot be found.
     */
    @Throws(UnknownHostException::class)
    private fun getLocalAddress(): InetAddress {
        try {
            var candidateAddress: InetAddress? = null
            // Iterate all NICs (network interface cards)...
            for(iface:NetworkInterface in NetworkInterface.getNetworkInterfaces()) {
                // Iterate all IP addresses assigned to each card...
                for (inetAddr:InetAddress in iface.inetAddresses) {
                    if (!inetAddr.isLoopbackAddress) {

                        if (inetAddr.isSiteLocalAddress) {
                            // Found non-loopback site-local address. Return it immediately
                            return inetAddr
                        } else if (candidateAddress == null) {
                            // Found non-loopback address, but not necessarily site-local.
                            // Store it as a candidate,
                            // to be returned if site-local address is not subsequently found
                            candidateAddress = inetAddr
                            // Note that we don't repeatedly assign non-loopback non-site-local addresses as candidates,
                            // only the first. For subsequent iterations, candidate will be non-null.
                        }
                    }
                }
            }
            // We did not find a site-local address, but we found some other non-loopback address.
            // Server might have a non-site-local address assigned to its NIC (or it might be running
            // IPv6 which deprecates the "site-local" concept).
            // Return this non-loopback candidate address...
            return candidateAddress ?: (InetAddress.getLocalHost()
                            ?: throw UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null."))
            //^ At this point, we did not find a non-loopback address.
            // Fall back to returning whatever InetAddress.getLocalHost() returns...
        } catch (e: Exception) {
            val unknownHostException = UnknownHostException("Failed to determine LAN address: $e")
            unknownHostException.initCause(e)
            throw unknownHostException
        }

    }
    /**
     * Finds the local IP address at compile-time,
     * and places it into a file named IpAddress.kt in <code>outputDir</code>.
     * @param outputDir directory where generated sources will be written to
     */
    fun generateCompileTimeLocalIpAddress(outputDir: File) {

        if (!outputDir.mkdirs() && !outputDir.isDirectory) {
            throw IOException ("FAILED to create director(y/ies) '$outputDir' for compile-time local ip address!")
        }
        File(outputDir, "IpAddress.kt").writeText(
                "const val COMPILE_TIME_IP_ADDRESS=\"${getLocalAddress().hostAddress}\"\n")
    }
}
