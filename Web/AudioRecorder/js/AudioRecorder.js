
       /* container for audio player*/ 
      var container = document.getElementById('container');
      /* field to show the audio recorder info */
      var recorderField = document.getElementById('audioRecorder');
      /* Messages for UI fields*/
      var AudioStartMessage = "Audio Recording STARTED....";
      var AudioStopMessage = "Audio Recording STOPPED....";

      /* method to show message in the audio message field in UI */
      function showMessage(message){
        recorderField.innerHTML = message;
      }

      /* file storing audio  time info*/
      var bTimeInfoFile;
      /* audio recording file name*/
      var bAudioTimeFile;
      /* recorder variable of type RecordRTC*/
      var recordAudio;
      /* indication for server that the data is audio time information */
      var recordingAction = "audio_time_info";
      /* server writer file*/
      var serverWriterFile = "FileWriter.php";

      var TimeEnum = {
          AUDIO_START: 1,
          AUDIO_STOP:2,
          AUDIO_FILE_SAVED:3
      };
      /*
      * method that triggers recordings
      * @param fileName1 file storing audio data
      * @param fileName2 file storing audio time information
      */
      function startAudioRecordings(fileName1, fileName2) {
        bAudioTimeFile = fileName1;
        bTimeInfoFile = fileName2;
        //audio-recorder
        navigator.getUserMedia = navigator.getUserMedia ||
               navigator.webkitGetUserMedia ||
               navigator.mozGetUserMedia;
        
        /* check if browser support media recording */       
        if(!navigator.getUserMedia){
          alert("navigator.getUserMedia not supported");
        }else{
            navigator.getUserMedia({
              audio: true,
              video:false
            }, function(stream) {
                  //preview.src = window.URL.createObjectURL(stream);
                  //preview.play();
                  // var legalBufferValues = [256, 512, 1024, 2048, 4096, 8192, 16384];
                  // sample-rates in at least the range 22050 to 96000.
                  recordAudio = RecordRTC(stream, {
                    //bufferSize: 16384,
                    //sampleRate: 44100,
                    onAudioProcessStarted: function() {
                       console.log('audio process started');
                    }
                  });
                  console.log('startRecording() : audio recording started');
                  //$("#div1").html('audio recording started:');
                  recordTimeInfo(recordingAction, TimeEnum.AUDIO_START, (new Date()).getTime());
                  recordAudio.startRecording();
                  showMessage(AudioStartMessage);
                  
              }, function(error) {
                alert(JSON.stringify(error, null, '\t'));
            });

        }
      }
      /* method to stop audio recording*/
      function stopAudioRecording(){
          recordAudio.stopRecording(function() {
          showMessage(AudioStopMessage);
          recordTimeInfo(recordingAction, TimeEnum.AUDIO_STOP ,(new Date()).getTime());
          
          /*
          var rate = recordAudio.sampleRate;
          console.log('SampleRate: ' + rate);
          */
          PostBlob(recordAudio.getBlob(), 'audio', bAudioTimeFile);
          
          //$("#div2").html('audio recordings saved:' + (new Date()).getTime());
          recordTimeInfo(recordingAction, TimeEnum.AUDIO_FILE_SAVED, (new Date()).getTime());
          console.log('stopRecording(): audio recording saved..');
          //recordTimeInfo('audio_time_info','Average Time Difference', getAvgTimeDiff());
          //recordTimeInfo('audio_time_info','Average Trip Time', getAvgTripTime());
            
            
          });
      }

      /** 
      * PostBlob method uses XHR2 and FormData to submit 
      * recorded blob to the PHP server
      * @param blob the blob data to be send to server
      * @param fileType type of file - audio, video, gif
      * @param fileName name of file to save the blob
      **/
      function PostBlob(blob, fileType, fileName) {
            // FormData
            var formData = new FormData();
            formData.append(fileType + '-filename', fileName);
            formData.append(fileType + '-blob', blob);

            // progress-bar
            var hr = document.createElement('hr');
            container.appendChild(hr);
            var strong = document.createElement('strong');
            strong.id = 'percentage';
            strong.innerHTML = fileType + ' upload progress: ';
            container.appendChild(strong);
            var progress = document.createElement('progress');
            container.appendChild(progress);

            // POST the Blob using XHR2
            xhr('save.php', formData, progress, percentage, function(fileURL) {
              container.appendChild(document.createElement('hr'));
              var mediaElement = document.createElement(fileType);

              var source = document.createElement('source');
              var href = location.href.substr(0, location.href.lastIndexOf('/') + 1);
              source.src = href + fileURL;
              console.log(source.src);
              if (fileType == 'audio') source.type = !!navigator.mozGetUserMedia ? 'audio/ogg' : 'audio/wav';

              mediaElement.appendChild(source);

              mediaElement.controls = true;
              container.appendChild(mediaElement);
              mediaElement.play(); //to play the recorded audio instantly

              progress.parentNode.removeChild(progress);
              strong.parentNode.removeChild(strong);
              hr.parentNode.removeChild(hr);
            });
      }

      function xhr(url, data, progress, percentage, callback) {
            var request = new XMLHttpRequest();
            request.onreadystatechange = function() {
                if (request.readyState == 4 && request.status == 200) {
                    callback(request.responseText);
                }
            };

            if (url.indexOf('delete.php') == -1) {
                request.upload.onloadstart = function() {
                    percentage.innerHTML = 'Upload started...';
                };

                request.upload.onprogress = function(event) {
                    progress.max = event.total;
                    progress.value = event.loaded;
                    percentage.innerHTML = 'Upload Progress ' + Math.round(event.loaded / event.total * 100) + "%";
                };

                request.upload.onload = function() {
                    percentage.innerHTML = 'Saved!';
                };
            }

            request.open('POST', url);
            request.send(data);
        }

      /**
      * method thats send time info to the server
      * @param _action the identifier to differentiate different events - key events, audio, gcm etc...
      * @param _infoType the type of info i.e. start, end recording
      * @param _eventTime the timestamp
      */
      function recordTimeInfo(_action, _infoType, _eventTime){
            console.log('action: ' + _action + " infoType: " + _infoType + " eventtime: " + _eventTime);
            dataToSend = {
                action  : _action,
                infoType : _infoType,
                eventTime : _eventTime,
                fileName : bTimeInfoFile
            };
            console.log("URL: " + serverWriterFile+ " dataTOsend: "+ dataToSend);
            
            $.ajax({
              type: "POST",
              url: serverWriterFile,
              data: dataToSend,
              success: function(resp_data) {
                console.log('recordTimeInfo: ' + resp_data);
              }
            });//end of .ajax({})
      }