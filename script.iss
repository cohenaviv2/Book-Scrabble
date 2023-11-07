[Setup]
AppName=Book Scrabble
AppVersion=1.0
DefaultDirName={pf}\BookScrabble
OutputDir=C:\InstallerOutput

[Files]
Source: "C:\Projects\Book Scrabble\*"; DestDir: "{app}"
Source: "C:\Program Files\Java\jdk1.8.0_351\jre\*"; DestDir: "{app}\jre"; Flags: ignoreversion recursesubdirs createallsubdirs
