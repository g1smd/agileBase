/*
 *  Copyright 2009 GT webMarque Ltd
 * 
 *  This file is part of GT portalBase.
 *
 *  GT portalBase is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  GT portalBase is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with GT portalBase.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
   Javascript object to handle XML requests triggered by entry into a text box.

   Object operates as follows:

   Keystroke event occurs on element -> Entry for the element in the queue buffer? -yes-> reset the timer for this entry in the queue -> Keystroke is the nth in the element -no-> end
                                                                  |                                                                         /|\
                                                                 no -> create entry in buffer -> set a timer for this entry in the queue    -

   Queue timer expires -> Object is in the sending buffer -no-> clear timer -> add to send queue -> set timer on send queue -> run change event for the element
                                     |
                                    yes -> end (timer is still running)

   XML request returns (from fChange) -> clear element from send queue -> clear timer  (fChange handles the behaviour of the element)

   Send timer expires -> Resend request

   Notes:
   1.  The value in the object isn't cached - the value sent is the value in the object when the request is made
 */

function fBuffer() {
	// object maintains a reference to a DOM element and tracks keypresses and
	// delays between keypresses
	// after a specified number of keypresses or a 2 second delay, fChange() is
	// called on the DOM element
	// to send an XML request to update the server with its new contents

	// wrap a DOM element as a BufferObject and add to the first empty space
	// within the Buffer array
	// (add it to the end if there are no empty slots)
	function fBufferObject(_oElement) {
		var oElement = _oElement;
		var iKeyCount = 0;
		var iTimerId = -1;
		var oChange;
		this.element = oElement;
		this.keyCount = iKeyCount;
		this.timerId = iTimerId;
		this.change = oChange;
	}

	function fAddElementToBuffer(oBufferObject, aBuffer) {

		function fCompressBuffer(aArray) {
			// remove any null elements from the end of the array
			// while the last element is null, remove it

			// NOTE: this is never called!
			while (aArray[aArray.length - 1] == null)
				aArray.pop();
		}

		function fFirstBlankElement(aArray) {
			// call fCompressBuffer here?
			for ( var i = 0; i < aArray.length; i++) {
				if (!aArray[i])
					return i;
			}
			return -1;
		}

		var iElementIndex = fFirstBlankElement(aBuffer);
		if (iElementIndex < 0) {
			aBuffer.push(oBufferObject);
			var iElementIndex = aBuffer.length - 1;
		} else {
			aBuffer[iElementIndex] = oBufferObject;
		}
		return iElementIndex;
	}

	function fWriteBuffer(oElementToBuffer) {
		// locate the index of the calling DOM element within the buffer array
		function fElementIndex() {
			for ( var i = 0; i < aQueueBuffer.length; i++) {
				if (aQueueBuffer[i]
						&& (aQueueBuffer[i].element == oElementToBuffer))
					return i;
			}
			return -1;
		}
		// clear the timer for a BufferObject if one exists
		// set an initial / new timer for the BufferObject
		function fSetTimer() {
			var iSendMS = 2500;
			if (oBufferObject.timerId >= 0)
				window.clearInterval(oBufferObject.timerId);
			var sInterval = 'top.oBuffer.send(' + iElementIndex + ')';
			oBufferObject.timerId = window.setInterval(sInterval, iSendMS);
		}

		// find the DOM element within the buffer array or add it if not already
		// present
		var iElementIndex = fElementIndex();
		if (iElementIndex < 0)
			iElementIndex = fAddElementToBuffer(new fBufferObject(
					oElementToBuffer), aQueueBuffer);
		var oBufferObject = aQueueBuffer[iElementIndex];
		// increment the keycount and reset/set the timer
		oBufferObject.keyCount++;
		fSetTimer();
		fEnableDisable('disable', oElementToBuffer);
		// if the send is also trigger by a count of keystrokes and this is the
		// nth stroke, try a send immediately
		// keep the timer trigger too in case the send queue isn't clear
		if ((oBufferObject.element.getAttribute('maxKeyCount'))
				&& (oBufferObject.keyCount
						% oBufferObject.element.getAttribute('maxKeyCount') == 0))
			fSend(iElementIndex);
	}

	// call fChange for the DOM element
	// clear timer & delete BufferObject wrapper
	// set occupied Buffer space as empty
	function fSend(iQueueElementIndex) {
		function fSetTimer() {
			var iTimeoutMS = 15000;
			if (oBufferObject.timerId >= 0)
				window.clearInterval(oBufferObject.timerId);
			var sInterval = 'top.oBuffer.resend(' + iSendElementIndex + ')';
			oBufferObject.timerId = window.setInterval(sInterval, iTimeoutMS);
		}

		function fQueueElementIndexInSendBuffer() {
			for ( var i = 0; i < aSendBuffer.length; i++) {
				if (aSendBuffer[i]
						&& (aSendBuffer[i].element == oBufferObject.element))
					return i;
			}
			return -1;
		}

		var oBufferObject = aQueueBuffer[iQueueElementIndex];
		// if the element in the send queue, don't do anything
		if (fQueueElementIndexInSendBuffer() >= 0)
			return;
		// clear the interval timer which is seeing whether the request can be
		// passed to the send buffer
		window.clearInterval(oBufferObject.timerId);
		oBufferObject.keyCount = 0;

		// add the queued element into the send buffer and remove from queue
		// buffer
		iSendElementIndex = fAddElementToBuffer(oBufferObject, aSendBuffer);
		aQueueBuffer[iQueueElementIndex] = null;

		// set an interval on the sent item to resend if no response is received
		fSetTimer();
		
		// this runs the request. change property is a handle to the xmlHTTP
		// request which is handling the data
		oBufferObject.change = new fChange(oBufferObject.element);
	}

	function fResend(iSendElementIndex) {
		// check that the number of resends hasn't been exceeded in a future
		// version maybe...

		iNumberOfResends = iNumberOfResends + 1;
		var numActiveQueueElements = fNumActiveElements(aSendBuffer);
		fDisplayResendMessage(numActiveQueueElements, iNumberOfResends);
		oBufferObject = aSendBuffer[iSendElementIndex];

		// abort the current request
		if (oBufferObject.change)
			oBufferObject.change.xml_http_request.abort();

		// check whether there is something in the queue and if so abort resend
		for ( var i = 0; i < aQueueBuffer.length; i++) {
			if (aQueueBuffer[i] && (aQueueBuffer[i] == oBufferObject)) {
				fClearFromSendQueue(oBufferObject.element);
				return;
			}
		}

		// this runs the request. change property is a handle to the xmlHTTP
		// request which is handling the data
		oBufferObject.change = new fChange(oBufferObject.element);
	}

	function fClearFromSendQueue(oDOMObject) {
		var iSendElementIndex = -1;	
		// try and find the element in the queue
		for ( var i = 0; i < aSendBuffer.length; i++) {
			if (aSendBuffer[i]) {			
				if (aSendBuffer[i].element == oDOMObject) {
					iSendElementIndex = i;				
				}
			}
		}			
		if (iSendElementIndex == -1) {
			return;
		}
		
		oBufferObject = aSendBuffer[iSendElementIndex];
		window.clearInterval(oBufferObject.timerId);
		aSendBuffer[iSendElementIndex] = null;

		// Count how many active (non-null) things we've got left in the send
		// queue.
		// If zero we know
		// a) any and all re-sends have completed
		// b) we can delete all send queue array elements
		var numActiveQueueElements = fNumActiveElements(aSendBuffer);
		if (numActiveQueueElements == 0)
			iNumberOfResends = 0;
		fDisplayResendMessage(numActiveQueueElements, iNumberOfResends);
	}

	function fDisplayResendMessage(iSendQueueLength, iNumberOfResends) {
		var resendMessage = '';
		if (iNumberOfResends > 0) {
			resendMessage = ' ';
			for ( var i = 0; i < iSendQueueLength; i++) {
				resendMessage = resendMessage + '-';
			}
			resendMessage = resendMessage + ' saving';
			for ( var i = 0; i < iNumberOfResends; i++) {
				resendMessage = resendMessage + '.';
			}
			resendMessage = resendMessage + ' ';
			for ( var i = 0; i < iSendQueueLength; i++) {
				resendMessage = resendMessage + '-';
			}
		}
		document.title = sNormalWindowTitle + resendMessage;
	}

	function fNumActiveElements(aArray) {
		var numActiveElements = 0;
		for (i = 0; i < aArray.length; i++) {
			if (aArray[i] != null) {
				numActiveElements = numActiveElements + 1;
			}
		}
		return numActiveElements;
	}

	// declare the buffer
	var aQueueBuffer = new Array();
	var aSendBuffer = new Array();
	// expose the functions for writing to the buffer and sending from a timer
	this.writeBuffer = fWriteBuffer;
	this.send = fSend;
	this.resend = fResend;
	this.clearFromSendQueue = fClearFromSendQueue;
	// to deal with notifications when network is slow
	var sNormalWindowTitle = document.title;
	var iNumberOfResends = 0;
}

var oBuffer = new fBuffer();
