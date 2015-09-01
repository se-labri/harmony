## Adding options to a source repository ##

You can add options in your source-config.json, for instance if you need an analysis to do a particular treatment on each source.

Here is an example of how to add options :

```
[
	{
		"url":"https://svn.apache.org/repos/asf/ant/core/trunk/",
		"class":"SvnKitSourceExtractor",
		"options": {
			"bug-files":["/defect/ant-1.3/ant-1.3.csv","/defect/ant-1.4/ant-1.4.csv","/defect/ant-1.5/ant-1.5.csv","/defect/ant-1.6/ant-1.6.csv","/defect/ant-1.7/ant-1.7.csv"],
			"snapshots-commits":["268584","269530","272630","275277","486471"]
		}
	},
	{
		"url":"https://svn.apache.org/repos/asf/camel/trunk/",
		"class":"SvnKitSourceExtractor",
		"options": {
			"bug-files":["/defect/camel-1.0/camel-1.0.csv","/defect/camel-1.2/camel-1.2.csv","/defect/camel-1.4/camel-1.4.csv","/defect/camel-1.6/camel-1.6.csv"],
			"snapshots-commits":["551220","583569","677560","743613"]
		}
	}
]
```

The options can be retrieved later in an analysis :

```

public void runOn(Source src) {
	ArrayList<String> bugFilesPaths = (ArrayList<String>) src.getConfig().getOption("bug-files");
	ArrayList<String> snapshotsNativeIds = (ArrayList<String>) src.getConfig().getOption("snapshots-commits");
}

```

In that case the options are of the type `ArrayList<String>`, but it can be of the following types:

```
 "my-option":"my-value"
```
The option is of type `String`.

```
 "my-option":true
```
The option is of type `boolean`.

```
  "my-option" : 42
```

The option is of type `int`