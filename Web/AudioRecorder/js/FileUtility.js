/**
* @param dir directory where all the recording files are stored.
* @timeVar timeStamp time stamp for uniqueness of filename
* @placeValue name of place where recordings are made
* @type type of file -  bTimeSync, bAudio, bAudioTime, bEvents
*/
function getFileName(dir, timeVar, placeValue, type){
  var fileName = dir + '/' + timeVar + '_' + placeValue + '_' + type;
  if(type == 'bAudio'){
    fileName += '.wav';
  }else{
    fileName += '.csv';
  }
  return fileName;
}
 
