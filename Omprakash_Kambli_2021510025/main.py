import os,sys,time,subprocess
import keyword,pkgutil,gi,uuid
gi.require_version('Wnck', '3.0')
import PyQt5
from gi.repository import (Wnck, Gdk)
from PyQt5.QtWidgets import *#(QSizePolicy,QTabWidget,QMainWindow,QApplication,QFrame,QVBoxLayout,QHBoxLayout,QLabel,QFileSystemModel,QTreeView,QSplitter,QWidget,QFileDialog)#*
from PyQt5.QtCore import *#(QModelIndex,QProcess,Qt,QDir)#*
from PyQt5.QtGui import *#(QKeyEvent,QFont,QWindow,QColor)#*
from PyQt5.Qsci import *#(QsciScintilla,QsciLexerPython)#*
from PyQt5 import Qsci
from pathlib import Path
from tempfile import (NamedTemporaryFile,mkstemp)
from io import StringIO

#Helper Functions

def addActionToMenu(menu,menuitem,shortcut,function) :
    menuitem = menu.addAction(menuitem)
    menuitem.setShortcut(shortcut)
    menuitem.triggered.connect(function)
    return menuitem

class Container(QTabWidget):
    def __init__(self):
        QTabWidget.__init__(self)
        self.embed('xterm')
    def embed(self, command, *args):
        self.name_session = uuid.uuid4().hex
        proc = QProcess()
        proc.setProgram(command)
        proc.setArguments(args)
        started, procId = QProcess.startDetached(
            "xterm", ["-e", "tmux", "new", "-s", self.name_session], "."
        )
        if not started:
            QMessageBox.critical(self, 'Command "{}" not started!'.format(command), "Eh")
            return
        attempts = 0
        while attempts < 10:
            screen = Wnck.Screen.get_default()
            screen.force_update()
            time.sleep(0.1)
            while Gdk.events_pending():
                Gdk.event_get()
            for w in screen.get_windows():
                if w.get_pid() == procId:
                    self.window = QWindow.fromWinId(w.get_xid())
                    proc.setParent(self)
                    win32w = QWindow.fromWinId(w.get_xid())
                    win32w.setFlags(Qt.FramelessWindowHint)
                    widg = QWidget.createWindowContainer(win32w)
                    self.addTab(widg, command)
                    return
            attempts += 1
        QMessageBox.critical(self, 'Window not found', 'Process started but window not found')

    def stop(self):
        QProcess.execute("tmux", ["kill-session", "-t", self.name_session])

    def send_command(self, command):
        QProcess.execute(
            "tmux", ["send-keys", "-t", self.name_session, command, "Enter"]
        )

class MainWindow(QMainWindow):
    def __init__(self):
        super(QMainWindow, self).__init__()
        self.process = QProcess(self)
        self.side_bar_clr = "#4a4a4a"
        self.init_ui()
        self.current_file = None

    def init_ui(self):
        self.setWindowTitle("pyRho")
        self.showMaximized()
        self.window_font = QFont()
        self.window_font.setPointSize(12)
        self.setFont(self.window_font)
        self.set_up_menu()
        self.set_up_body()
        self.statusBar().showMessage("PyRho build 0.04")
        self.show()

    def set_up_menu(self):
        menu_bar = self.menuBar()
        file_menu = menu_bar.addMenu("File")
        new_file = file_menu.addAction("New")
        new_file.setShortcut("Ctrl+N")
        new_file.triggered.connect(self.new_file)
        open_file = file_menu.addAction("Open File")
        open_file.setShortcut("Ctrl+O")
        open_file.triggered.connect(self.open_file)
        open_folder = file_menu.addAction("Open Folder")
        open_folder.setShortcut("Ctrl+K")
        open_folder.triggered.connect(self.open_folder)
        file_menu.addSeparator()
        save_file = file_menu.addAction("Save")
        save_file.setShortcut("Ctrl+S")
        save_file.triggered.connect(self.save_file)
        save_as = file_menu.addAction("Save As")
        save_as.setShortcut("Ctrl+Shift+S")
        save_as.triggered.connect(self.save_as)
        edit_menu = menu_bar.addMenu("Edit")
        copy_action = edit_menu.addAction("Copy")
        copy_action.setShortcut("Ctrl+C")
        copy_action.triggered.connect(self.copy)
        run_action= menu_bar.addAction("Run")
        run_action.setShortcut("F5")
        run_action.triggered.connect(self.run_file_fromeditor)
        btfy_btn= menu_bar.addAction("Beautify")
        btfy_btn.setShortcut("F7")
        btfy_btn.triggered.connect(self.beautify)
        m=addActionToMenu(file_menu,"Close","Alt+F4",exit)

    def get_editor(self) -> QsciScintilla:
        editor = QsciScintilla()
        editor.setUtf8(True)
        editor.setFont(self.window_font)
        editor.setBraceMatching(QsciScintilla.SloppyBraceMatch)

        editor.setIndentationGuides(True)
        editor.setTabWidth(4)
        editor.setIndentationsUseTabs(False)
        editor.setAutoIndent(True)

        editor.setAutoCompletionSource(QsciScintilla.AcsAll)
        editor.setAutoCompletionThreshold(1) 
        editor.setAutoCompletionCaseSensitivity(False)
        editor.setAutoCompletionUseSingle(QsciScintilla.AcusNever)

        editor.setCaretLineVisible(True)
        editor.setCaretWidth(3)

        editor.setEolMode(QsciScintilla.EolWindows)
        editor.setEolVisibility(False)
        self.pylexer = QsciLexerPython()
        self.pylexer.setDefaultFont(self.window_font)

        editor.setLexer(self.pylexer)
        api=Qsci.QsciAPIs(self.pylexer)
        pyqt_path = os.path.dirname(PyQt5.__file__)
        api.load(os.path.join(pyqt_path, "Qt5/qsci/api/python/Python-3.8.api"))
        api.prepare()
        editor.setMarginType(0, QsciScintilla.NumberMargin)
        editor.setMarginWidth(0, "00000")
        editor.setMarginsForegroundColor(QColor("#ff888888"))
        editor.setMarginsBackgroundColor(QColor("#282c34"))
        editor.setMarginsFont(self.window_font)
        editor.keyPressEvent = self.handle_editor_press
        return editor

    def handle_editor_press(self, e: QKeyEvent):
        editor: QsciScintilla = self.tab_view.currentWidget()
        if e.modifiers() == Qt.ControlModifier and e.key() == Qt.Key_Space:
            editor.autoCompleteFromAll()
        else:
            QsciScintilla.keyPressEvent(editor, e)

    def is_binary(self, path):
        '''
        Check if file is binary
        '''
        with open(path, 'rb') as f:
            return b'\0' in f.read(1024)


    def set_new_tab(self, path: Path, is_new_file=False):
        editor = self.get_editor()
        
        if is_new_file:
            self.tab_view.addTab(editor, "untitled.py")
            self.setWindowTitle("untitled.py")
            self.statusBar().showMessage("Opened untitled.py")
            self.tab_view.setCurrentIndex(self.tab_view.count() - 1)
            self.current_file = None
            return
        
        if not path.is_file():
            return
        if self.is_binary(path):
            self.statusBar().showMessage("Cannot Open Binary File", 2000)
            return

        for i in range(self.tab_view.count()):
            if self.tab_view.tabText(i) == path.name:
                self.tab_view.setCurrentIndex(i)
                self.current_file = path
                return

        self.tab_view.addTab(editor, path.name)
        editor.setText(path.read_text())
        self.setWindowTitle(path.name)
        self.current_file = path
        self.tab_view.setCurrentIndex(self.tab_view.count() - 1)
        self.statusBar().showMessage(f"Opened {path.name}", 2000)

    def set_up_body(self):    
        self.body_frame = QFrame()
        self.body_frame.setFrameShape(QFrame.NoFrame)
        self.body_frame.setFrameShadow(QFrame.Plain)
        self.body_frame.setLineWidth(0)
        self.body_frame.setMidLineWidth(0)
        self.body_frame.setContentsMargins(0, 0, 0, 0)
        self.body_frame.setSizePolicy(QSizePolicy.Expanding, QSizePolicy.Expanding)
        
        self.body = QVBoxLayout()
        self.body.setContentsMargins(0, 0, 0, 0)
        self.body.setSpacing(0)
        self.body_frame.setLayout(self.body)

        self.side_bar = QFrame()
        self.side_bar.setFrameShape(QFrame.StyledPanel)
        self.side_bar.setFrameShadow(QFrame.Plain)
        self.side_bar.setStyleSheet(f'''
            background-color: {self.side_bar_clr};
        ''')   
        side_bar_layout = QHBoxLayout()
        side_bar_layout.setContentsMargins(5, 10, 5, 0)
        side_bar_layout.setSpacing(0)
        side_bar_layout.setAlignment(Qt.AlignTop | Qt.AlignCenter)

        folder_label = QLabel()
        folder_label.setAlignment(Qt.AlignmentFlag.AlignTop)
        folder_label.setFont(self.window_font)
        side_bar_layout.addWidget(folder_label)
        self.side_bar.setLayout(side_bar_layout)
  
        self.tree_frame = QFrame()
        self.tree_frame.setLineWidth(1)
        self.tree_frame.setMaximumWidth(400)
        self.tree_frame.setMinimumWidth(200)
        self.tree_frame.setBaseSize(100, 0)
        self.tree_frame.setContentsMargins(0, 0, 0, 0)
        
        tree_frame_layout = QVBoxLayout()
        tree_frame_layout.setContentsMargins(0, 0, 0, 0)
        tree_frame_layout.setSpacing(0)
        self.tree_frame.setStyleSheet('''
            QFrame {
                background-color: #21252b;
                border-radius: 5px;
                border: none;
                padding: 5px;
                color: #D3D3D3;
            }
            QFrame:hover {
                color: white;
            }
        ''')
        self.model = QFileSystemModel()
        self.model.setRootPath(os.getcwd())
        self.model.setFilter(QDir.NoDotAndDotDot | QDir.AllDirs | QDir.Files)

        self.tree_view = QTreeView()
        
        self.tree_view.setModel(self.model)
        self.tree_view.setRootIndex(self.model.index(os.getcwd()))
        self.tree_view.setSelectionMode(QTreeView.SingleSelection)
        self.tree_view.setSelectionBehavior(QTreeView.SelectRows)
        self.tree_view.setEditTriggers(QTreeView.NoEditTriggers)
        self.tree_view.clicked.connect(self.tree_view_clicked)
        self.tree_view.setIndentation(10)
        self.tree_view.setSizePolicy(QSizePolicy.Expanding, QSizePolicy.Expanding)
        
        self.tree_view.setHeaderHidden(True)
        self.tree_view.setColumnHidden(1, True)
        self.tree_view.setColumnHidden(2, True)
        self.tree_view.setColumnHidden(3, True)

        tree_frame_layout.addWidget(self.tree_view)
        self.tree_frame.setLayout(tree_frame_layout)

        self.tab_view = QTabWidget()
        self.tab_view.setContentsMargins(0, 0, 0, 0)
        self.tab_view.setTabsClosable(True)
        self.tab_view.setMovable(True)
        self.tab_view.setDocumentMode(True)
        self.tab_view.tabCloseRequested.connect(self.close_tab)
        
        self.hsplit = QSplitter(Qt.Horizontal)
        self.vsplit = QSplitter(Qt.Vertical)
        
        self.hsplit.addWidget(self.tree_frame)
        self.hsplit.addWidget(self.tab_view)
        
        self.hsplit.addWidget(self.vsplit)

        self.terminal=Container()

        self.terminal.setMaximumHeight(200)
        
        self.body.addWidget(self.hsplit)
        self.body.addWidget(self.terminal)
        
        self.body_frame.setLayout(self.body)
        self.setCentralWidget(self.body_frame)

    def close_tab(self, index):
        self.tab_view.removeTab(index)

    def tree_view_clicked(self, index: QModelIndex):
        path = self.model.filePath(index)
        p = Path(path)
        self.set_new_tab(p)
       
    def new_file(self):
        self.set_new_tab(None, is_new_file=True)

    def save_file(self):
        if self.current_file is None and self.tab_view.count() > 0:
            self.save_as()
        
        editor = self.tab_view.currentWidget()
        self.current_file.write_text(editor.text())
        self.statusBar().showMessage(f"Saved {self.current_file.name}", 2000)
   
    def save_as(self):
        editor = self.tab_view.currentWidget()
        if editor is None:
            return
        ispyth=False
        file_path = QFileDialog.getSaveFileName(self, "Save As", os.getcwd(),"*.py")[0]
        pyexs=[".py",".pyx",".pyc",".pyw",".pyd",".pyi",".xpy",".pyp",".pyz"]
        for i in pyexs:
            if(file_path.endswith(i)==1):
                ispyth=True
            else:
                pass
        
        if file_path == '':
            self.statusBar().showMessage("Cancelled", 2000)
            return
        if(not ispyth):
            file_path+=".py" 
        path = Path(file_path)
        path.write_text(editor.text())
        self.tab_view.setTabText(self.tab_view.currentIndex(), path.name)
        self.statusBar().showMessage(f"Saved {path.name}", 2000)
        self.current_file = path


    def open_file(self):
        ops = QFileDialog.Options()
        ops |= QFileDialog.DontUseNativeDialog
        new_file, _ = QFileDialog.getOpenFileName(self,
                    "Pick A File", "", "All Files (*);;Python Files (*.py)",
                    options=ops)
        if new_file == '':
            self.statusBar().showMessage("Cancelled", 2000)
            return
        f = Path(new_file)
        self.set_new_tab(f)

    def open_folder(self):
        ops = QFileDialog.Options()
        ops |= QFileDialog.DontUseNativeDialog
        
        new_folder = QFileDialog.getExistingDirectory(self, "Pick A Folder", "", options=ops)
        if new_folder:
            self.model.setRootPath(new_folder)
            self.tree_view.setRootIndex(self.model.index(new_folder))
            self.statusBar().showMessage(f"Opened {new_folder}", 2000)
    def copy(self):
        editor = self.tab_view.currentWidget()
        if editor is not None:
            editor.copy()
    def run_file_fromeditor(self):
        editor = self.tab_view.currentWidget()        
        if editor is not None:
            self.currfile=editor.text()
            scriptFile = NamedTemporaryFile(suffix='.py',delete=False)
            with open(scriptFile.name,'w') as f:
                f.write(self.currfile)
            os.chmod(scriptFile.name,0o0777)
            try:
                self.terminal.send_command("python3 "+scriptFile.name)
            except:
                pass
            scriptFile.file.close()
    def beautify(self):
        editor = self.tab_view.currentWidget()
        if editor is not None:
            self.currfile=editor.text()
            scriptFile = NamedTemporaryFile(suffix='.py',delete=False)
            with open(scriptFile.name,'w') as f:
                f.write(self.currfile)
            #cmd="autopep8 --agressive "
            op=subprocess.Popen(['autopep8','-a',scriptFile.name],stdout=subprocess.PIPE,universal_newlines=True).communicate()[0]
            #op=pc1.stdout.read()
            #print(op)
##            retval=os.system(cmd)
##            try:
##                self.terminal.send_command("autopep8 --in-place "+scriptFile.name)
##            except:
##                pass
            editor.setText(op)
if __name__ == '__main__':
    app = QApplication([])
    window = MainWindow()
    sys.exit(app.exec())