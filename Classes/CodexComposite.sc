CodexComposite {
	classvar <directory, id = 'sc-modules', <cache;
	var <moduleSet, <modules;

	*initClass {
		Class.initClassTree(Dictionary);
		Class.initClassTree(CodexPaths);
		directory = CodexPaths.at(id) ?? {
			CodexPaths.setAt(this.defaultDirectory, id);
		};
		cache = CodexCache.new;
		this.allSubclasses.do({ | class |
			Class.initClassTree(class);
			class.checkDefaults;
		});
	}

	*new { | moduleSet(\default), from |
		^super.newCopyArgs(moduleSet)
		.loadModules(from).initComposite;
	}

	*getModules { | set, from |
		if(this.notAt(set) and: { this.shouldAdd(set, from) }, {
			this.addModules(set);
		});
		^cache.modulesAt(this.name, set);
	}

	*notAt { | set | ^cache.notAt(this.name, set); }

	*shouldAdd { | set, from |
		if(from.notNil, {
			this.copyModules(set, from);
			forkIfNeeded { this.processFolders(set, from) };
			^false;
		}, { this.processFolders(set); ^true });
	}

	*copyModules { | to, from |
		if(this.notAt(from), { this.addModules(from) });
		cache.copyEntry(this.name, from, to);
	}

	*loadScripts { | at |
		var return = ();
		this.asPath(at).getScriptPaths.do({ | script |
			return.add(this.scriptKey(script) -> script.load);
		});
		^return;
	}

	*asPath { | input |
		input = input.asString;
		if(PathName(input).isRelativePath, {
			^(this.classFolder+/+input);
		}, { ^input; });
	}

	*classFolder { ^(this.directory +/+ this.name); }

	*scriptKey { | input |
		^PathName(input).fileNameWithoutExtension
		.lowerFirstChar.asSymbol;
	}

	*processFolders { | set, from |
		var folder = this.asPath(set);
		if(folder.exists.not, {
			folder.mkdir;
			from !? { this.copyFiles(from, folder) } ?? { this.template(folder) };
		});
	}

	*copyFiles { | from, to |
		from = this.asPath(from);
		if(from.exists, {
			from.copyScriptsTo(to);
		}, { this.processTemplates(to) });
	}

	*template { | where |
		this.makeTemplates(CodexTemplater(this.asPath(where)));
	}

	*makeTemplates { | templater | }

	*addModules { | moduleSymbol |
		cache.add(this.name, moduleSymbol, this.loadScripts(moduleSymbol));
	}

	*defaultDirectory {
		^(Platform.userExtensionDir.dirname+/+id);
	}

	*checkDefaults {
		var defaults = this.defaultModulesPath;
		var folder = this.classFolder+/+"default";
		if(defaults.exists && folder.exists.not, {
			defaults.copyScriptsTo(folder.mkdir);
		});
	}

	*defaultModulesPath { ^""; }

	loadModules { | from |
		modules = this.class.getModules(moduleSet, from);
	}

	initComposite {}

	moduleFolder { ^(this.class.classFolder+/+moduleSet); }

	reloadScripts {
		cache.removeModules(this.name, moduleSet);
		this.moduleSet = moduleSet;
	}

	reloadModules { this.moduleSet = moduleSet }

	moduleSet_{ | newSet, from |
		moduleSet = newSet;
		this.loadModules(from);
		this.initComposite;
	}

	*moduleSets {
		^PathName(this.classFolder).folders
		.collectAs({ | m | m.folderName.asSymbol }, Set);
	}

	moduleSets { ^this.class.moduleSets }

	*directory_{| newPath |
		directory = CodexPaths.setAt(newPath, id);
	}

	name { ^this.class.name; }

	openModules {
		var ide = Platform.ideName;
		case { ide=="scqt"} { this.openModulesSCqt }
		{ ide=="scnvim" }{
			var shell = "echo $SHELL".unixCmdGetStdOut.split($/).last;
			shell = shell[..(shell.size - 2)];
			this.openModulesSCVim(shell, true, true);
		}
		{ ide=="scvim" }{
			var shell = "echo $SHELL".unixCmdGetStdOut.split($/).last;
			shell = shell[..(shell.size - 2)];
			this.openModulesSCVim(shell, false, true);
		}
		{ this.openModulesSCqt }
	}

	openModulesSCqt {
		if(\Document.asClass.notNil, {
			PathName(this.moduleFolder).files.do{ | file |
				\Document.asClass.perform(\open, file.fullPath);
			}
		});
	}

	openModulesSCVim { | shell("sh"), neovim = false, vertically = false |
		var cmd = "vim", paths = PathName(this.moduleFolder)
		.files.collect(_.fullPath);
		if(neovim, { cmd = $n++cmd });
		if(vertically, { cmd = cmd+" -o "}, { cmd = cmd+" -O " });
		paths.do{ | path | cmd=cmd++path++" " };
		if(\GnomeTerminal.asClass.notNil, {
			cmd.perform(\runInGnomeTerminal, shell);
		}, { cmd.perform(\runInTerminal, shell) });
	}

}
