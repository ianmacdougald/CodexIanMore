CompositeExample : Composite { 
	var routine, pattern;

	//If you want to initialize variables but don't want to rewrite Composite's constructor, 
	//use the otherwise empty method initComposite.
	initComposite {}

	//The only actual requirement for developing Composite-typed classes is to define the modules that make up the composite. 
	//This is done in the method makeTemplater. Composite holds an instance of Templater called templater for this purpose.
	//If a specific template is not available, extend Templater to make it.
	makeTemplates { 
		templater.pattern( "sequence1" ); 
		templater.pattern( "sequence2" ); 
		templater.pattern( "sequence3" );
	}

	//This is an example of a kind of behavior one can develop—playing three patterns in a routine. 
	//Note that the class assumes that the modules defined in makeTemplates exist with the same names and with the same types. 
	//However, how they exist is entirely up to the user...
	play { 
		routine = fork{ 
			pattern = modules.sequence1.play;
			8.wait; 
			pattern.stop; 
			pattern = modules.sequence2.play;
			32.wait; 
			pattern.stop;
			pattern = modules.sequence3.play;
			32.wait; 
			pattern.stop;
		};
	}

	//This would stop the speculative routine defined above. 
	stop { 
		routine.stop; 
		pattern.stop; 
	}

}