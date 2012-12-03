package org.wavetuner

import org.wavetuner.programs.WaveTunerPrograms
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ProgramDetailFragment._
import scala.collection.JavaConversions._
import android.widget.ImageButton
import org.wavetuner.programs.NeuroFeedbackProgram
import android.view.WindowManager
import android.scala.reactive.AndroidDomain._
import org.wavetuner.eeg.SessionRecorder
import android.scala.reactive.AndroidDomain

object ProgramDetailFragment {

  val ARG_ITEM_ID = "item_id"
}

class ProgramDetailFragment extends Fragment with ListenerConversions with Observing {

  var mItem: NeuroFeedbackProgram = _
  val started = EventSource[Boolean]
  val stopped = EventSource[Boolean]

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    if (getArguments.containsKey(ARG_ITEM_ID)) {
      mItem = WaveTunerPrograms.programsByName.get(getArguments.getString(ARG_ITEM_ID))
    }
  }

  override def onStop {
    super.onStop
    stopped << true
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val rootView = inflater.inflate(R.layout.fragment_program_detail, container, false)
    val textView = rootView.findViewById(R.id.program_detail).asInstanceOf[TextView]
    if (mItem != null) {
      textView
        .setText(mItem.toString())
    }
    val btnPlay = rootView.findViewById(R.id.btnPlay).asInstanceOf[ImageButton]

    SessionRecorder.forRawData(WaveTunerPrograms.measurement).listen(started, stopped)
    mItem.observeRunStateChanges(started, stopped)
    var isRunning = false
    btnPlay.setOnClickListener { v: View =>
      if (!isRunning) {
        btnPlay.setImageDrawable(getResources().getDrawable(R.drawable.img_btn_pause))
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        started << true
        isRunning = true
      } else {
        btnPlay.setImageDrawable(getResources().getDrawable(R.drawable.img_btn_play))
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        stopped << true
        isRunning = false
      }
      AndroidDomain.engine.runTurn
    }
    rootView
  }
}
