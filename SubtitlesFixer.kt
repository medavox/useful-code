package com.github.medavox.kubjson

import java.io.File
import kotlin.math.roundToLong

/**stretches the overall time of all timestamps in an SRT by the specified coefficient.
coefficient can be between 0 and 1 to slow them down, and > 1 to speed them up.
 You can also provide an offset, which is in milliseconds.

 It also merges adjacent duplicate subtitles.
 When two adjacent subtitle entries have exactly the same text,
it replace the end time of the first with the end time of the second,
and omits the second.*/

//val coefficient = 0.925619834 // = 121 minute runtime of my copy, versus 112 minute apparent runtime of SRT
val multiplier = 0.96 //guesstimate
val offset:Long = 2000 //in milliseconds
val inFile = File("in.srt")
val outFile = File("out.srt")

private data class Timestamp(val hours:Long, val minutes:Long, val seconds:Long, val millis:Long) {
    companion object {
        /**Creates an instance from a string of the subtitle time format.*/
        fun from(s:String):Timestamp {
            val validation = Regex("(\\d\\d):(\\d\\d):(\\d\\d),(\\d{3})")
            val matches:Sequence<MatchResult> = validation.findAll(s)
            require(matches.count() == 1) { "input string \"$s\" is invalid!" }
            val groups = matches.first().groups
            val hours = groups[1]?.value?.toLong() ?: throw IllegalArgumentException("hours was null from input string \"$s\"")
            val minutes = groups[2]?.value?.toLong() ?: throw IllegalArgumentException("minutes was null from input string \"$s\"")
            val seconds = groups[3]?.value?.toLong() ?: throw IllegalArgumentException("seconds was null from input string \"$s\"")
            val millis = groups[4]?.value?.toLong() ?: throw IllegalArgumentException("millis was null from input string \"$s\"")
            return Timestamp(
                hours = hours,
                minutes = minutes,
                seconds = seconds,
                millis = millis
            )
        }

        const val millisInHour = 3600_000
        const val millisInMinute = 60_000
        const val millisInSecond = 1_000
        /**Creates an instance from the specified number of milliseconds since zero.*/
        fun ofMillis(millis: Long):Timestamp {
            var inProgress = millis
            val hours = inProgress / millisInHour
            inProgress -= (hours * millisInHour)

            val minutes = inProgress / millisInMinute
            inProgress -= (minutes * millisInMinute)

            val seconds = inProgress / millisInSecond
            inProgress -= (seconds * millisInSecond)

            return Timestamp(hours, minutes, seconds, inProgress)
        }
    }
    private val timeFormatString = "%1$02d:%2$02d:%3$02d,%4$03d"
    /**Gets the timestamp, formatted in the SRT time format.*/
    fun format():String {
        return String.format(timeFormatString, hours, minutes, seconds, millis)
    }
    /**Gets the timestamp in milliseconds since zero*/
    fun toMillis():Long {
        return (hours*3600_000)+(minutes*60_000)+(seconds*1000)+millis
    }
}
private data class SubtitleEntry(
    val number:String,
    val startTime:Timestamp,
    val endTime:Timestamp,
    val text:String
) {
    override fun toString(): String {
        return number+"\n"+
                startTime.format()+" --> "+endTime.format()+"\n"+
                text+"\n"
    }
}

enum class State {
    NUMBER,
    TIME,
    TEXT
}

fun main() {
    //example entry:
    //1
    //00:00:41,762 --> 00:00:45,140
    //Our holiday home, Summer 1978

    //read in existing file
    val entries = mutableListOf<SubtitleEntry>()
    var lastSeenStartAndEnd:String = ""
    var subTextAppender:String = ""
    var subCounter:Int = 1
    var currentState = State.NUMBER
    loop@ for(line in inFile.readLines()) {
        when(currentState) {
            State.NUMBER -> {
                //check that the read-in counter matches what we expected
                val actual = line.toInt()
                check(actual == subCounter) {
                    "expected and actual sub counts don't match. " +
                            "Expected: $subCounter; actual: $actual"
                }
                currentState = State.TIME
            }
            State.TIME -> {
                lastSeenStartAndEnd = line
                currentState = State.TEXT
            }
            State.TEXT -> {
                if(line.isNotEmpty()) {
                    subTextAppender += line+"\n"
                    //keep reading text until we see a blank line, signifying the entry's end
                    continue@loop
                }else {
                    val startAndEnd = lastSeenStartAndEnd.split(" --> ")
                    val startTime = Timestamp.from(startAndEnd[0])
                    val endTime = Timestamp.from(startAndEnd[1])
                    entries.add(SubtitleEntry(subCounter.toString(), startTime, endTime, subTextAppender))
                    subTextAppender = ""
                    subCounter++
                    currentState = State.NUMBER
                }
            }
        }
    }

    //write out modified data to file
    //firstly, wipe any existing data in it
    outFile.writeText("")
    var subIndex = 1
    var nextDupeEnd:Timestamp? = null
    var shouldSkip = false
    for(i in entries.indices) {
        if(shouldSkip) {
            shouldSkip = false
            continue
        }
        if(i < entries.size-1 && entries[i+1].text == entries[i].text) {//if an entry's been duplicated,
            //use the later one's time as the end
            nextDupeEnd = entries[i+1].endTime
            shouldSkip = true
        }
        val modifiedStart:Long = (entries[i].startTime.toMillis() / multiplier).roundToLong() + offset
        val modifiedEnd:Long = ( (nextDupeEnd ?: entries[i].endTime).toMillis() / multiplier).roundToLong() + offset
        val modifiedEntry = SubtitleEntry(subIndex.toString(),
            Timestamp.ofMillis(modifiedStart),
            Timestamp.ofMillis(modifiedEnd), entries[i].text)
        outFile.appendText(modifiedEntry.toString())
        //outFile.appendText(entries[i].toString())
        subIndex++
        if(nextDupeEnd != null) {
            nextDupeEnd = null
        }
    }
}
