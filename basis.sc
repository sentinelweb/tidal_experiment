include("SuperDirt")

SuperDirt.start
(
// configure the sound server: here you could add hardware specific options
// see http://doc.sccode.org/Classes/ServerOptions.html
// https://github.com/musikinformatik/SuperDirt
s.options.sampleRate = 44100; // increase this if you need to load more samples
s.options.numBuffers = 1024 * 16; // increase this if you need to load more samples
s.options.memSize = 8192 * 16; // increase this if you get "alloc failed" messages
s.options.maxNodes = 1024 * 32; // increase this if you are getting drop outs and the message "too many nodes"
s.options.numOutputBusChannels = 2; // set this to your hardware output channel size, if necessary`
s.options.numInputBusChannels = 2; // set this to your hardware input channel size, if necessary
// s.options.outDevice ="DDJ-400 Audio Out";
// s.options.outDevice="Soundflower (2ch)";
s.options.outDevice="Robert Munro’s Beats Studi";
// s.options.outDevice="Built-in Output";
// s.options.inDevice="Built-in Output";
// boot the server and start SuperDirt
s.waitForBoot {
	~dirt = SuperDirt(2, s); // two output channels, increase if you want to pan across more channels
	// for example: ~dirt.loadSoundFiles("/Users/myUserName/Dirt/samples/*");
	~dirt.loadSoundFiles; // load samples (path containing a wildcard can be passed in)

	// wait for samples to be read
	s.sync;

	// / start listening on port 57120, create 8 orbits, each sending audio to channel 0. You can direct sounds to the orbits from tidal e.g. by: `# orbit "0 1 1"
	~dirt.start(57120, [0, 0, 0, 0, 0, 0, 0, 0]);

	// https://tidalcycles.org/index.php/SuperDirt_MIDI_Tutorial
	// to map midi FROM tidal
	MIDIClient.init;
	// ~midiOut = MIDIOut.newByName("USB Oxygen 8 v2", "USB Oxygen 8 v2");
	//~midiOut = MIDIOut.newByName("BCF2000", "BCF2000");
	// ~midiOut = MIDIOut.newByName("DDJ-1000", "DDJ-1000");
	~midiOut = MIDIOut.newByName("IAC Driver", "IAC Bus");
	//~midiOut.latency = 0.45;
	~dirt.soundLibrary.addMIDI(\midi, ~midiOut);
}


)

// to map midi INTO tidal - mapping MIDI -> OSC.
(
var on, off, cc;
var osc;

osc = NetAddr.new("127.0.0.1", 6010);

MIDIClient.init;
MIDIIn.connectAll;
~noteOn = { |val, num, chan, src|
	osc.sendMsg("/ctrl", num.asString, val/127);
};
on = MIDIFunc.noteOn(~noteOn);

~noteOff = { |val, num, chan, src|
	osc.sendMsg("/ctrl", num.asString, 0);
};
off = MIDIFunc.noteOff(~noteOff);

~cc = { |val, num, chan, src|
	osc.sendMsg("/ctrl", num.asString, val/127);
};
cc = MIDIFunc.cc(~cc);

if (~stopMidiToOsc != nil, {
	~stopMidiToOsc.value;
});

~stopMidiToOsc = {
	on.free;
	off.free;
	cc.free;
};
)

(
MIDIIn.removeFuncFrom(\noteOn, ~noteOn);
MIDIIn.removeFuncFrom(\noteOff, ~noteOff);
MIDIIn.removeFuncFrom(\cc, ~cc);
)


// Evaluate the line below to stop it.
~stopMidiToOsc.value;




