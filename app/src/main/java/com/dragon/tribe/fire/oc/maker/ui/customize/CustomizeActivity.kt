package com.dragon.tribe.fire.oc.maker.ui.customize

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.dragon.tribe.fire.oc.maker.R
import com.dragon.tribe.fire.oc.maker.core.base.BaseActivity
import com.dragon.tribe.fire.oc.maker.core.dialog.ConfirmDialog
import com.dragon.tribe.fire.oc.maker.core.extensions.dLog
import com.dragon.tribe.fire.oc.maker.core.extensions.eLog
import com.dragon.tribe.fire.oc.maker.core.extensions.handleBack
import com.dragon.tribe.fire.oc.maker.core.extensions.hideNavigation
import com.dragon.tribe.fire.oc.maker.core.extensions.invisible
import com.dragon.tribe.fire.oc.maker.core.extensions.onSingleClick
import com.dragon.tribe.fire.oc.maker.core.extensions.showInterAll
import com.dragon.tribe.fire.oc.maker.core.extensions.showToast
import com.dragon.tribe.fire.oc.maker.core.extensions.startIntent
import com.dragon.tribe.fire.oc.maker.core.extensions.visible
import com.dragon.tribe.fire.oc.maker.core.helper.MediaHelper
import com.dragon.tribe.fire.oc.maker.core.utils.SaveState
import com.dragon.tribe.fire.oc.maker.core.utils.SystemUtils.setLocale
import com.dragon.tribe.fire.oc.maker.core.utils.key.IntentKey
import com.dragon.tribe.fire.oc.maker.core.utils.key.ValueKey
import com.dragon.tribe.fire.oc.maker.data.custom.ItemNavCustomModel
import com.dragon.tribe.fire.oc.maker.data.model.SuggestionModel
import com.dragon.tribe.fire.oc.maker.databinding.ActivityCustomizeBinding
import com.dragon.tribe.fire.oc.maker.ui.background1.BackgroundActivity
import com.dragon.tribe.fire.oc.maker.ui.home.DataViewModel
import com.lvt.ads.util.Admob
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CustomizeActivity : BaseActivity<ActivityCustomizeBinding>() {
    private val viewModel: CustomizeViewModel by viewModels()
    private val dataViewModel: DataViewModel by viewModels()
    val colorLayerCustomizeAdapter by lazy { ColorLayerAdapter(this) }
    val layerCustomizeAdapter by lazy { CustomizeLayerAdapter(this) }
    val bottomNavigationCustomizeAdapter by lazy { BottomNavigationAdapter(this) }
    val hideList: ArrayList<View> by lazy {
        arrayListOf(
            binding.btnRandom, binding.layoutRcvColor, binding.rcvLayer, binding.flBottomNav
        )
    }

    override fun setViewBinding(): ActivityCustomizeBinding {
        return ActivityCustomizeBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initRcv()
        lifecycleScope.launch { showLoading() }
        dataViewModel.ensureData(this)
        binding.txtNext.isSelected = true
    }

    override fun dataObservable() {
        // allData
        lifecycleScope.launch {
            dataViewModel.allData.collect { list ->
                if (list.isNotEmpty()) {
                    viewModel.positionSelected = intent.getIntExtra(IntentKey.INTENT_KEY, 0)
                    viewModel.statusFrom =
                        intent.getIntExtra(IntentKey.STATUS_FROM_KEY, ValueKey.CREATE)
                    viewModel.setDataCustomize(list[viewModel.positionSelected])
                    viewModel.setIsDataAPI(viewModel.positionSelected >= ValueKey.POSITION_API)
                    initData()
                }
            }
        }

        // isFlip
        lifecycleScope.launch {
            viewModel.isFlip.collect { status ->
                val rotation = if (status) -180f else 0f
                // chỉ thao tác nếu imageViewList đã được khởi tạo và có cùng kích thước
                if (viewModel.imageViewList.isNotEmpty()) {
                    viewModel.imageViewList.forEach { view ->
                        view.rotationY = rotation
                    }
                }
            }
        }
        lifecycleScope.launch {
            viewModel.isHideView.collect { status ->
                if (viewModel.isCreated.value) {
                    if (!status) {
                        hideList.forEach { it.visible() }
                        checkStatusColor()
                    } else {
                        hideList.forEach { it.invisible() }
                    }
                }
            }
        }


        // bottomNavigationList
        // bottomNavigationList
        lifecycleScope.launch {
            viewModel.bottomNavigationList.collect { bottomNavigationList ->
                if (bottomNavigationList.isNotEmpty()) {
                    bottomNavigationCustomizeAdapter.submitList(bottomNavigationList)
                    layerCustomizeAdapter.submitList(viewModel.itemNavList[viewModel.positionNavSelected])
                    colorLayerCustomizeAdapter.submitList(viewModel.colorItemNavList[viewModel.positionNavSelected])
                    if (viewModel.colorItemNavList[viewModel.positionNavSelected].isNotEmpty()) {
                        binding.rcvColor.smoothScrollToPosition(viewModel.colorItemNavList[viewModel.positionNavSelected].indexOfFirst { it.isSelected })
                    }
                    checkStatusColor()
                }
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            actionBar.apply {
                btnBack.onSingleClick(1500) { confirmExit() }
                btnNext.onSingleClick(1500) { handleSave() }
            }
            btnRandom.onSingleClick(1000) { handleRandomAllLayer() }
            btnReset.onSingleClick(1500) { handleReset() }
            btnFlip.onSingleClick(1000) { viewModel.setIsFlip() }

        }
        handleRcv()
    }

    override fun initText() {

    }


    private fun initRcv() {
        binding.apply {
            rcvLayer.apply {
                adapter = layerCustomizeAdapter
                itemAnimator = null
            }

            rcvColor.apply {
                adapter = colorLayerCustomizeAdapter
                itemAnimator = null
            }

            rcvNavigation.apply {
                adapter = bottomNavigationCustomizeAdapter
                itemAnimator = null
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            }
        }
    }

    private fun handleRcv() {
        layerCustomizeAdapter.onItemClick = { item, position ->
            viewModel.checkDataInternet(this) {
                handleFillLayer(
                    item, position
                )
            }
        }

        layerCustomizeAdapter.onNoneClick =
            { position -> viewModel.checkDataInternet(this) { handleNoneLayer(position) } }

        layerCustomizeAdapter.onRandomClick =
            { viewModel.checkDataInternet(this) { handleRandomLayer() } }

        colorLayerCustomizeAdapter.onItemClick =
            { position -> viewModel.checkDataInternet(this) { handleChangeColorLayer(position) } }

        bottomNavigationCustomizeAdapter.onItemClick = { positionBottomNavigation ->
            viewModel.checkDataInternet(this) {
                handleClickBottomNavigation(
                    positionBottomNavigation
                )
            }
        }
    }

    private fun initData() {
        val handleExceptionCoroutine = CoroutineExceptionHandler { _, throwable ->
            eLog("initData: ${throwable.message}")
            CoroutineScope(Dispatchers.Main).launch {
                dismissLoading(true)
                val dialogExit = ConfirmDialog(
                    this@CustomizeActivity, R.string.error, R.string.an_error_occurred
                )
                dialogExit.show()
                dialogExit.onNoClick = {
                    dialogExit.dismiss()
                    finish()
                }
                dialogExit.onYesClick = {
                    dialogExit.dismiss()
                    hideNavigation()
                    startIntent(CustomizeActivity::class.java, viewModel.positionSelected)
                    finish()
                }
            }
        }

        CoroutineScope(SupervisorJob() + Dispatchers.IO + handleExceptionCoroutine).launch {
            var pathImageDefault = ""
            // Get data from list
            val deferred1 = async {
                when (viewModel.statusFrom) {
                    ValueKey.CREATE -> {
                        viewModel.resetDataList()
                        viewModel.addValueToItemNavList()
                        viewModel.setItemColorDefault()
                        viewModel.setFocusItemNavDefault()
                    }

                    // Edit
                    else -> {
                        viewModel.updateSuggestionModel(
                            MediaHelper.readListFromFile<SuggestionModel>(
                                this@CustomizeActivity, ValueKey.SUGGESTION_FILE_INTERNAL
                            ).first()
                        )
                        viewModel.fillSuggestionToCustomize()
                    }
                }

                // đảm bảo positionNavSelected và positionCustom được set
                viewModel.setPositionCustom(viewModel.dataCustomize.value!!.layerList.first().positionCustom)
                viewModel.setPositionNavSelected(viewModel.dataCustomize.value!!.layerList.first().positionNavigation)
                viewModel.setBottomNavigationListDefault()
                dLog("deferred1")
                return@async true
            }
            // Add custom view in FrameLayout
            val deferred2 = async(Dispatchers.Main) {
                if (deferred1.await()) {
                    viewModel.setImageViewList(binding.layoutCustomLayer)
                    dLog("deferred2")
                }
                return@async true
            }

            // Fill data default
            val deferred3 = async {
                if (deferred1.await() && deferred2.await()) {
                    if (viewModel.statusFrom == ValueKey.CREATE) {
                        pathImageDefault =
                            viewModel.dataCustomize.value!!.layerList.first().layer.first().image
                        viewModel.setIsSelectedItem(viewModel.positionCustom)
                        viewModel.setPathSelected(viewModel.positionCustom, pathImageDefault)
                        viewModel.setKeySelected(viewModel.positionNavSelected, pathImageDefault)
                    }
                    dLog("deferred3")
                }
                return@async true
            }

            withContext(Dispatchers.Main) {
                if (deferred1.await() && deferred2.await() && deferred3.await()) {
                    when (viewModel.statusFrom) {
                        ValueKey.CREATE -> {
                            // Bảo vệ: kiểm tra index hợp lệ
                            val pos = viewModel.positionCustom
                            if (pos in viewModel.imageViewList.indices) {
                                Glide.with(this@CustomizeActivity).load(pathImageDefault)
                                    .into(viewModel.imageViewList[pos])
                            }
                        }

                        // Edit
                        else -> {
                            viewModel.pathSelectedList.forEachIndexed { index, path ->
                                if (path != "" && index in viewModel.imageViewList.indices) {
                                    Glide.with(this@CustomizeActivity).load(path)
                                        .into(viewModel.imageViewList[index])
                                }
                            }
                        }
                    }

                    // đảm bảo positionNavSelected hợp lệ trước khi gọi adapters
                    val posNav = viewModel.positionNavSelected
                    if (posNav in 0 until viewModel.itemNavList.size) {
                        layerCustomizeAdapter.submitList(viewModel.itemNavList[posNav])
                    }
                    if (posNav in 0 until viewModel.colorItemNavList.size) {
                        colorLayerCustomizeAdapter.submitList(viewModel.colorItemNavList[posNav])
                    }

                    checkStatusColor()
                    viewModel.setIsCreated(true)
                    dismissLoading()
                    dLog("main")
                }
            }
        }
    }

    private fun checkStatusColor() {
        val pos = viewModel.positionNavSelected
        if (pos in viewModel.colorItemNavList.indices && viewModel.colorItemNavList[pos].isNotEmpty()) {
            if (viewModel.isShowColorList.getOrNull(pos) == true) {
                binding.layoutRcvColor.visible()
            } else {
                binding.layoutRcvColor.invisible()
            }
        } else {
            binding.layoutRcvColor.invisible()
        }
    }


    private fun handleFillLayer(item: ItemNavCustomModel, position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val pathSelected = viewModel.setClickFillLayer(item, position)
            withContext(Dispatchers.Main) {
                val pos = viewModel.positionCustom
                if (pos in viewModel.imageViewList.indices) {
                    Glide.with(this@CustomizeActivity).load(pathSelected)
                        .into(viewModel.imageViewList[pos])
                }
                layerCustomizeAdapter.submitList(viewModel.itemNavList.getOrNull(viewModel.positionNavSelected)!!)
            }
        }
    }

    private fun handleNoneLayer(position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.setIsSelectedItem(viewModel.positionCustom)
            viewModel.setPathSelected(viewModel.positionCustom, "")
            viewModel.setKeySelected(viewModel.positionNavSelected, "")
            viewModel.setItemNavList(viewModel.positionNavSelected, position)
            withContext(Dispatchers.Main) {
                val pos = viewModel.positionCustom
                if (pos in viewModel.imageViewList.indices) {
                    Glide.with(this@CustomizeActivity).clear(viewModel.imageViewList[pos])
                }
                layerCustomizeAdapter.submitList(viewModel.itemNavList.getOrNull(viewModel.positionNavSelected)!!)
            }
        }
    }

    private fun handleRandomLayer() {
        lifecycleScope.launch(Dispatchers.IO) {
            val (pathRandom, isMoreColors) = viewModel.setClickRandomLayer()
            withContext(Dispatchers.Main) {
                val pos = viewModel.positionCustom
                if (pos in viewModel.imageViewList.indices) {
                    Glide.with(this@CustomizeActivity).load(pathRandom)
                        .into(viewModel.imageViewList[pos])
                }
                layerCustomizeAdapter.submitList(viewModel.itemNavList.getOrNull(viewModel.positionNavSelected)!!)
                if (isMoreColors) {
                    colorLayerCustomizeAdapter.submitList(viewModel.colorItemNavList[viewModel.positionNavSelected])
                    binding.rcvColor.smoothScrollToPosition(viewModel.colorItemNavList[viewModel.positionNavSelected].indexOfFirst { it.isSelected })
                }
            }
        }
    }

    private fun handleChangeColorLayer(position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val pathColor = viewModel.setClickChangeColor(position)
            withContext(Dispatchers.Main) {
                if (pathColor != "") {
                    val pos = viewModel.positionCustom
                    if (pos in viewModel.imageViewList.indices) {
                        Glide.with(this@CustomizeActivity).load(pathColor)
                            .into(viewModel.imageViewList[pos])
                    }
                }
                colorLayerCustomizeAdapter.submitList(viewModel.colorItemNavList[viewModel.positionNavSelected])

            }
        }
    }

    private fun handleClickBottomNavigation(positionBottomNavigation: Int) {
        if (positionBottomNavigation == viewModel.positionNavSelected) return

        lifecycleScope.launch(Dispatchers.IO) {
            // Cập nhật state trong ViewModel

            viewModel.setPositionNavSelected(positionBottomNavigation)
            viewModel.setPositionCustom(viewModel.dataCustomize.value!!.layerList[positionBottomNavigation].positionCustom)
            viewModel.setClickBottomNavigation(positionBottomNavigation)

            // Không reload lại toàn bộ nav, chỉ update focus
            withContext(Dispatchers.Main) {
                showInterAll {
                    bottomNavigationCustomizeAdapter.select(positionBottomNavigation)
                    layerCustomizeAdapter.submitList(viewModel.itemNavList[viewModel.positionNavSelected])
                    colorLayerCustomizeAdapter.submitList(viewModel.colorItemNavList[viewModel.positionNavSelected])
                    checkStatusColor()
                }
            }
        }
    }


    private fun confirmExit() {
        val dialog = ConfirmDialog(
            this, R.string.exit, R.string.haven_t_saved_it_yet_do_you_want_to_exit, true
        )
        setLocale(this)
        dialog.show()
        dialog.onYesClick = {
            dialog.dismiss()
            hideNavigation()

            showInterAll {
                handleBack()
            }
        }
        dialog.onNoClick = {
            dialog.dismiss()
            hideNavigation()
        }
    }

    private fun handleSave() {
        viewModel.checkDataInternet(this@CustomizeActivity) {
            lifecycleScope.launch {

                viewModel.saveImageFromView(this@CustomizeActivity, binding.layoutCustomLayer)
                    .collect { result ->
                        when (result) {
                            is SaveState.Loading -> showLoading()
                            is SaveState.Error -> {
                                dismissLoading(true)
                                showToast(R.string.save_failed_please_try_again)
                            }

                            is SaveState.Success -> {
                                dismissLoading(true)
                                    val savedPath = result.path
                                    val selectedBackground = viewModel.selectedBackgroundPath ?: ""

                                    val intent = Intent(
                                        this@CustomizeActivity, BackgroundActivity::class.java
                                    )
                                    intent.putExtra(IntentKey.PREVIOUS_IMAGE_KEY, savedPath)
                                    intent.putExtra(
                                        IntentKey.BACKGROUND_IMAGE_KEY, selectedBackground
                                    )
                                showInterAll {
                                startActivity(intent)
                                }
                            }
                        }
                    }
            }
        }
    }

    private fun handleReset() {
        val dialog = ConfirmDialog(
            this@CustomizeActivity, R.string.reset, R.string.change_your_whole_design_are_you_sure
        )
        setLocale(this)
        dialog.show()
        dialog.onYesClick = {
            viewModel.checkDataInternet(this) {
                dialog.dismiss()
                lifecycleScope.launch(Dispatchers.IO) {
                    val pathDefault = viewModel.setClickReset()
                    withContext(Dispatchers.Main) {

                            viewModel.imageViewList.forEach { imageView ->
                                Glide.with(this@CustomizeActivity).clear(imageView)
                            }
                            val pos =
                                viewModel.dataCustomize.value!!.layerList.first().positionCustom
                            if (pos in viewModel.imageViewList.indices) {
                                Glide.with(this@CustomizeActivity).load(pathDefault)
                                    .into(viewModel.imageViewList[pos])
                            }
                            layerCustomizeAdapter.submitList(
                                viewModel.itemNavList.getOrNull(
                                    viewModel.positionNavSelected
                                )!!
                            )
                            if (viewModel.positionNavSelected in viewModel.colorItemNavList.indices) {
                                colorLayerCustomizeAdapter.submitList(viewModel.colorItemNavList[viewModel.positionNavSelected])
                            }
                        showInterAll {
                        hideNavigation()
                        }
                    }
                }
            }
        }
        dialog.onNoClick = {
            dialog.dismiss()
            hideNavigation()
        }
    }

    private fun handleRandomAllLayer() {
        viewModel.checkDataInternet(this@CustomizeActivity) {
            lifecycleScope.launch(Dispatchers.IO) {
                val timeStart = System.currentTimeMillis()
                val isOutTurn = viewModel.setClickRandomFullLayer()

                withContext(Dispatchers.Main) {
                    viewModel.pathSelectedList.forEachIndexed { index, path ->
                        if (path.isNotEmpty() && index in viewModel.imageViewList.indices) {
                            Glide.with(this@CustomizeActivity).load(path)
                                .into(viewModel.imageViewList[index])
                        } else if (index in viewModel.imageViewList.indices && path.isEmpty()) {
                            Glide.with(this@CustomizeActivity).clear(viewModel.imageViewList[index])
                        }
                    }
                    layerCustomizeAdapter.submitList(viewModel.itemNavList.getOrNull(viewModel.positionNavSelected)!!)
                    if (viewModel.positionNavSelected in viewModel.colorItemNavList.indices) {
                        colorLayerCustomizeAdapter.submitList(viewModel.colorItemNavList[viewModel.positionNavSelected])
                    }
                    showInterAll {
                        if (isOutTurn) binding.btnRandom.invisible()
                        val timeEnd = System.currentTimeMillis()
                        dLog("time random all : ${timeEnd - timeStart}")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.setIsCreated(false)
    }

    override fun initAds() {
        super.initAds()
        Admob.getInstance()
            .loadNativeCollap(this, getString(R.string.native_collap_custom), binding.nativeAds2)
    }

    override fun onRestart() {
        super.onRestart()
        Admob.getInstance()
            .loadNativeCollap(this, getString(R.string.native_collap_custom), binding.nativeAds2)
    }

}
