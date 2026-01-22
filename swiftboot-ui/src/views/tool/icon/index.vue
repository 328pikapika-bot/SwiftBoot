<template>
  <div class="icon-page">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <div class="search-header">
        <div class="title">
          <el-icon :size="24"><Pointer /></el-icon>
          <span>Element Plus 图标库</span>
        </div>
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索图标名称..."
            clearable
            :prefix-icon="Search"
            style="width: 280px"
          />
        </div>
      </div>
      <div class="tips">
        <el-tag type="info" effect="plain">
          <el-icon><InfoFilled /></el-icon>
          点击图标即可复制名称，用于菜单管理中的图标配置
        </el-tag>
        <el-tag type="success" effect="plain">共 {{ filteredIcons.length }} 个图标</el-tag>
      </div>
    </el-card>

    <!-- 图标展示区域 -->
    <el-card shadow="never" class="icon-card">
      <div class="icon-grid">
        <div
          v-for="icon in filteredIcons"
          :key="icon.name"
          class="icon-item"
          @click="copyIconName(icon.name)"
        >
          <div class="icon-wrapper">
            <el-icon :size="28">
              <component :is="icon.component" />
            </el-icon>
          </div>
          <span class="icon-name">{{ icon.name }}</span>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-if="filteredIcons.length === 0" description="未找到匹配的图标" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, shallowRef } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Search, InfoFilled, Pointer,
  // 常用图标
  User, Lock, Menu, Setting, HomeFilled, House,
  // 箭头方向
  ArrowLeft, ArrowRight, ArrowUp, ArrowDown, Back, Right, Top, Bottom,
  ArrowLeftBold, ArrowRightBold, ArrowUpBold, ArrowDownBold,
  DArrowLeft, DArrowRight, CaretLeft, CaretRight, CaretTop, CaretBottom,
  // 操作
  Plus, Minus, Close, Check, Select, CloseBold, SemiSelect,
  Edit, Delete, Search as SearchIcon, Refresh, RefreshLeft, RefreshRight,
  ZoomIn, ZoomOut, FullScreen, ScaleToOriginal,
  Upload, Download, Sort, SortUp, SortDown,
  Copy, CopyDocument, Connection, Scissor,
  // 提示
  Warning, WarningFilled, CircleCheck, CircleCheckFilled,
  CircleClose, CircleCloseFilled, CirclePlus, CirclePlusFilled,
  Remove, RemoveFilled, QuestionFilled, InfoFilled as Info,
  SuccessFilled, Failed,
  // 媒体
  VideoPlay, VideoPause, VideoCamera, VideoCameraFilled,
  Microphone, Mute, Picture, PictureFilled, Camera, CameraFilled,
  // 文档
  Document, DocumentAdd, DocumentChecked, DocumentCopy, DocumentDelete, DocumentRemove,
  Folder, FolderAdd, FolderChecked, FolderDelete, FolderOpened, FolderRemove,
  Files, Collection, Reading, Tickets, Notebook, Memo, List,
  // 数据
  DataAnalysis, DataBoard, DataLine, Histogram, PieChart, TrendCharts,
  // 通讯
  Message, ChatDotRound, ChatLineRound, ChatDotSquare, ChatLineSquare, ChatSquare, ChatRound,
  Comment, Bell, BellFilled, Service, Headset, Phone, PhoneFilled,
  // 位置
  Location, LocationFilled, LocationInformation, Place, MapLocation, AddLocation, DeleteLocation,
  Position, Coordinate, Guide, Flag,
  // 天气时间
  Sunny, MostlyCloudy, Cloudy, PartlyCloudy, Moon, Lightning,
  Clock, Timer, Stopwatch, AlarmClock, Calendar, Watch,
  // 食物
  Apple, Grape, Cherry, Pear, Orange, Coffee, CoffeeCup, Dessert, IceCream, Food, Burger,
  KnifeFork, Chicken, Bowl, Dish, DishDot, Sugar, Goblet, GobletFull, GobletSquare, GobletSquareFull,
  // 其他物品
  Present, Trophy, Medal, FirstAidKit, Cpu, Monitor, MagicStick, Magnet,
  Key, Unlock, PriceTag, Discount, Wallet, Money, BankCard, Box, ShoppingBag, ShoppingCart, ShoppingCartFull,
  Goods, GoodsFilled, ShoppingTrolley, SoldOut, Sell,
  // 工具
  Tools, Brush, Crop, MostlyCloudy as Mosaic, EditPen, Aim,
  Rank, Grid, Help, Operation, Opportunity, OfficeBuilding,
  School, Shop, TakeawayBox, Suitcase, SuitcaseLine,
  // 交通
  Van, Ship, Bicycle, TurnOff,
  // 人物
  User as UserIcon, UserFilled, Avatar, Postcard, Male, Female,
  // 系统
  Monitor as MonitorIcon, Iphone, Cellphone, Printer, Platform,
  Link, Eleme, ElemeFilled, Share, Connection as ConnectionIcon,
  Switch, SwitchButton, SwitchFilled, Open, TurnOff as TurnOffIcon,
  // 品牌
  ChromeFilled, Promotion, Stamp,
  // 其他
  Star, StarFilled, View, Hide, Expand, Fold,
  More, MoreFilled, Filter, SetUp, Finished, Management,
  Loading, Cloudy as Loading2
} from '@element-plus/icons-vue'

// 图标列表
const iconList = shallowRef([
  // 常用图标
  { name: 'user', component: User },
  { name: 'lock', component: Lock },
  { name: 'menu', component: Menu },
  { name: 'setting', component: Setting },
  { name: 'home-filled', component: HomeFilled },
  { name: 'house', component: House },
  { name: 'search', component: SearchIcon },
  { name: 'edit', component: Edit },
  { name: 'delete', component: Delete },
  { name: 'plus', component: Plus },
  { name: 'minus', component: Minus },
  { name: 'close', component: Close },
  { name: 'check', component: Check },
  { name: 'refresh', component: Refresh },
  
  // 箭头方向
  { name: 'arrow-left', component: ArrowLeft },
  { name: 'arrow-right', component: ArrowRight },
  { name: 'arrow-up', component: ArrowUp },
  { name: 'arrow-down', component: ArrowDown },
  { name: 'back', component: Back },
  { name: 'right', component: Right },
  { name: 'top', component: Top },
  { name: 'bottom', component: Bottom },
  { name: 'd-arrow-left', component: DArrowLeft },
  { name: 'd-arrow-right', component: DArrowRight },
  { name: 'caret-left', component: CaretLeft },
  { name: 'caret-right', component: CaretRight },
  { name: 'caret-top', component: CaretTop },
  { name: 'caret-bottom', component: CaretBottom },
  
  // 操作
  { name: 'select', component: Select },
  { name: 'close-bold', component: CloseBold },
  { name: 'refresh-left', component: RefreshLeft },
  { name: 'refresh-right', component: RefreshRight },
  { name: 'zoom-in', component: ZoomIn },
  { name: 'zoom-out', component: ZoomOut },
  { name: 'full-screen', component: FullScreen },
  { name: 'scale-to-original', component: ScaleToOriginal },
  { name: 'upload', component: Upload },
  { name: 'download', component: Download },
  { name: 'sort', component: Sort },
  { name: 'sort-up', component: SortUp },
  { name: 'sort-down', component: SortDown },
  { name: 'copy', component: Copy },
  { name: 'copy-document', component: CopyDocument },
  { name: 'scissor', component: Scissor },
  
  // 提示状态
  { name: 'warning', component: Warning },
  { name: 'warning-filled', component: WarningFilled },
  { name: 'circle-check', component: CircleCheck },
  { name: 'circle-check-filled', component: CircleCheckFilled },
  { name: 'circle-close', component: CircleClose },
  { name: 'circle-close-filled', component: CircleCloseFilled },
  { name: 'circle-plus', component: CirclePlus },
  { name: 'circle-plus-filled', component: CirclePlusFilled },
  { name: 'remove', component: Remove },
  { name: 'remove-filled', component: RemoveFilled },
  { name: 'question-filled', component: QuestionFilled },
  { name: 'info-filled', component: Info },
  { name: 'success-filled', component: SuccessFilled },
  
  // 媒体
  { name: 'video-play', component: VideoPlay },
  { name: 'video-pause', component: VideoPause },
  { name: 'video-camera', component: VideoCamera },
  { name: 'video-camera-filled', component: VideoCameraFilled },
  { name: 'microphone', component: Microphone },
  { name: 'mute', component: Mute },
  { name: 'picture', component: Picture },
  { name: 'picture-filled', component: PictureFilled },
  { name: 'camera', component: Camera },
  { name: 'camera-filled', component: CameraFilled },
  
  // 文档
  { name: 'document', component: Document },
  { name: 'document-add', component: DocumentAdd },
  { name: 'document-checked', component: DocumentChecked },
  { name: 'document-copy', component: DocumentCopy },
  { name: 'document-delete', component: DocumentDelete },
  { name: 'document-remove', component: DocumentRemove },
  { name: 'folder', component: Folder },
  { name: 'folder-add', component: FolderAdd },
  { name: 'folder-checked', component: FolderChecked },
  { name: 'folder-delete', component: FolderDelete },
  { name: 'folder-opened', component: FolderOpened },
  { name: 'folder-remove', component: FolderRemove },
  { name: 'files', component: Files },
  { name: 'collection', component: Collection },
  { name: 'reading', component: Reading },
  { name: 'tickets', component: Tickets },
  { name: 'notebook', component: Notebook },
  { name: 'memo', component: Memo },
  { name: 'list', component: List },
  
  // 数据图表
  { name: 'data-analysis', component: DataAnalysis },
  { name: 'data-board', component: DataBoard },
  { name: 'data-line', component: DataLine },
  { name: 'histogram', component: Histogram },
  { name: 'pie-chart', component: PieChart },
  { name: 'trend-charts', component: TrendCharts },
  
  // 通讯
  { name: 'message', component: Message },
  { name: 'chat-dot-round', component: ChatDotRound },
  { name: 'chat-line-round', component: ChatLineRound },
  { name: 'chat-dot-square', component: ChatDotSquare },
  { name: 'chat-line-square', component: ChatLineSquare },
  { name: 'comment', component: Comment },
  { name: 'bell', component: Bell },
  { name: 'bell-filled', component: BellFilled },
  { name: 'service', component: Service },
  { name: 'headset', component: Headset },
  { name: 'phone', component: Phone },
  { name: 'phone-filled', component: PhoneFilled },
  
  // 位置
  { name: 'location', component: Location },
  { name: 'location-filled', component: LocationFilled },
  { name: 'location-information', component: LocationInformation },
  { name: 'place', component: Place },
  { name: 'map-location', component: MapLocation },
  { name: 'add-location', component: AddLocation },
  { name: 'delete-location', component: DeleteLocation },
  { name: 'position', component: Position },
  { name: 'coordinate', component: Coordinate },
  { name: 'guide', component: Guide },
  { name: 'flag', component: Flag },
  
  // 天气时间
  { name: 'sunny', component: Sunny },
  { name: 'mostly-cloudy', component: MostlyCloudy },
  { name: 'cloudy', component: Cloudy },
  { name: 'partly-cloudy', component: PartlyCloudy },
  { name: 'moon', component: Moon },
  { name: 'clock', component: Clock },
  { name: 'timer', component: Timer },
  { name: 'stopwatch', component: Stopwatch },
  { name: 'alarm-clock', component: AlarmClock },
  { name: 'calendar', component: Calendar },
  { name: 'watch', component: Watch },
  
  // 购物商业
  { name: 'price-tag', component: PriceTag },
  { name: 'discount', component: Discount },
  { name: 'wallet', component: Wallet },
  { name: 'money', component: Money },
  { name: 'bank-card', component: BankCard },
  { name: 'box', component: Box },
  { name: 'shopping-bag', component: ShoppingBag },
  { name: 'shopping-cart', component: ShoppingCart },
  { name: 'shopping-cart-full', component: ShoppingCartFull },
  { name: 'goods', component: Goods },
  { name: 'goods-filled', component: GoodsFilled },
  { name: 'sold-out', component: SoldOut },
  { name: 'sell', component: Sell },
  
  // 工具建筑
  { name: 'tools', component: Tools },
  { name: 'brush', component: Brush },
  { name: 'crop', component: Crop },
  { name: 'edit-pen', component: EditPen },
  { name: 'aim', component: Aim },
  { name: 'rank', component: Rank },
  { name: 'grid', component: Grid },
  { name: 'help', component: Help },
  { name: 'operation', component: Operation },
  { name: 'office-building', component: OfficeBuilding },
  { name: 'school', component: School },
  { name: 'shop', component: Shop },
  { name: 'suitcase', component: Suitcase },
  
  // 人物
  { name: 'user-filled', component: UserFilled },
  { name: 'avatar', component: Avatar },
  { name: 'postcard', component: Postcard },
  { name: 'male', component: Male },
  { name: 'female', component: Female },
  
  // 设备系统
  { name: 'monitor', component: MonitorIcon },
  { name: 'iphone', component: Iphone },
  { name: 'cellphone', component: Cellphone },
  { name: 'printer', component: Printer },
  { name: 'platform', component: Platform },
  { name: 'link', component: Link },
  { name: 'share', component: Share },
  { name: 'connection', component: Connection },
  { name: 'switch', component: Switch },
  { name: 'switch-button', component: SwitchButton },
  
  // 其他
  { name: 'star', component: Star },
  { name: 'star-filled', component: StarFilled },
  { name: 'view', component: View },
  { name: 'hide', component: Hide },
  { name: 'expand', component: Expand },
  { name: 'fold', component: Fold },
  { name: 'more', component: More },
  { name: 'more-filled', component: MoreFilled },
  { name: 'filter', component: Filter },
  { name: 'set-up', component: SetUp },
  { name: 'finished', component: Finished },
  { name: 'management', component: Management },
  { name: 'loading', component: Loading },
  { name: 'key', component: Key },
  { name: 'unlock', component: Unlock },
  { name: 'present', component: Present },
  { name: 'trophy', component: Trophy },
  { name: 'medal', component: Medal },
  { name: 'cpu', component: Cpu },
  { name: 'magic-stick', component: MagicStick },
  { name: 'promotion', component: Promotion },
  { name: 'stamp', component: Stamp },
])

const searchKeyword = ref('')

// 过滤后的图标
const filteredIcons = computed(() => {
  if (!searchKeyword.value) {
    return iconList.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return iconList.value.filter(icon => icon.name.includes(keyword))
})

// 复制图标名称
const copyIconName = async (name: string) => {
  try {
    await navigator.clipboard.writeText(name)
    ElMessage.success(`已复制: ${name}`)
  } catch {
    // 降级方案
    const input = document.createElement('input')
    input.value = name
    document.body.appendChild(input)
    input.select()
    document.execCommand('copy')
    document.body.removeChild(input)
    ElMessage.success(`已复制: ${name}`)
  }
}
</script>

<style lang="scss" scoped>
.icon-page {
  .search-card {
    margin-bottom: 16px;
    
    .search-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      .title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 18px;
        font-weight: 600;
        color: #303133;
      }
    }
    
    .tips {
      display: flex;
      gap: 12px;
      
      .el-tag {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
  }
  
  .icon-card {
    .icon-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(110px, 1fr));
      gap: 12px;
    }
    
    .icon-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 16px 8px;
      border-radius: 8px;
      background: #fafafa;
      cursor: pointer;
      transition: all 0.2s ease;
      border: 1px solid transparent;
      
      &:hover {
        background: #ecf5ff;
        border-color: #409eff;
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
        
        .icon-wrapper {
          color: #409eff;
        }
        
        .icon-name {
          color: #409eff;
        }
      }
      
      &:active {
        transform: translateY(0);
      }
      
      .icon-wrapper {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 48px;
        height: 48px;
        margin-bottom: 8px;
        color: #606266;
        transition: color 0.2s ease;
      }
      
      .icon-name {
        font-size: 12px;
        color: #909399;
        text-align: center;
        word-break: break-all;
        line-height: 1.4;
        transition: color 0.2s ease;
      }
    }
  }
}
</style>
