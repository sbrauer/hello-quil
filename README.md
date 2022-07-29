# hello-quil

A playground for exploring quil, including some examples that receive MIDI input.
Some sloppy code and WIP.
Generally you'll want to open a file in your editor and evaluate the file.
A window (possibly fullscreen, depends on the :size setting in the sketch) should popup.
Click on the window to focus it, and hit Esc to close it.
Some examples just draw something.  Some accept keyboard input.  Some are driven by midi events.

Note that the midi stuff assumes you're on a Mac and created a virtual IAC device named "IAC Driver IAC Bus 1".
You could change the `midi-device-description` in the file to use a different device on your system.
