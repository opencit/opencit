#!/usr/bin/perl
use strict;
my $VERSION = '0.1';
my $COPYRIGHT = 'Copyright (C) 2013 Intel Corporation';
my $LICENSE = 'All rights reserved.';
my %status = ( 'OK' => 0, 'WARNING' => 1, 'CRITICAL' => 2, 'UNKNOWN' => 3 );

# look for required modules
exit $status{UNKNOWN} unless load_modules(qw/Getopt::Long/);

# get options from command line
Getopt::Long::Configure("bundling");
my $verbose = 0;
my $help = "";
my $help_usage = "";
my $show_version = "";
my $host = "";
my $mtwilson_url = "";
my $asset_tag_url = "";
my @header = ();
my $delay_warn = 95;
my $delay_crit = 300;
my $timeout = "";
my @alert_plugins = ();
my @plugins = ();
my @token_formats = ();
my $tokenfile = "";
my $default_crit = 30;
my $default_warn = 15;
my $default_wait = 5;
my $default_timeout = 60;
my $libexec = "/usr/local/nagios/libexec";
my $ok;
$ok = Getopt::Long::GetOptions(
	"V|version"=>\$show_version,
	"v|verbose+"=>\$verbose,"h|help"=>\$help,"usage"=>\$help_usage,
	"w|warning=s"=>\$delay_warn,"c|critical=s"=>\$delay_crit, "t|timeout=s"=>\$timeout,
	"libexec=s"=>\$libexec,
	# plugin settings
	"p|plugin=s"=>\@plugins, "T|token=s"=>\@token_formats,
	"A|alert=i"=>\@alert_plugins,
	"F|file=s"=>\$tokenfile,
	# common settings
	"H|hostname=s"=>\$host,
	# mtwilson settings
	"mtwilson-url=s"=>\$mtwilson_url,
	"asset-tag-url=s"=>\$asset_tag_url,
	"header=s"=>\@header
	);

if( $show_version ) {
	print "$VERSION\n";
	if( $verbose ) {
		print "Warning threshold: $delay_warn seconds\n";
		print "Critical threshold: $delay_crit seconds\n";
		print "Default wait: $default_wait seconds\n";
		print "Default timeout: $default_timeout seconds\n";
	}
	exit $status{UNKNOWN};
}

if( $help ) {
	exec "perldoc", $0 or print "Try `perldoc $0`\n";
	exit $status{UNKNOWN};
}

if( $help_usage
	||
	(
		scalar(@plugins) == 0
		&&
		(
			$mtwilson_url eq "" || $asset_tag_url eq "" || $host eq "" 
		)
	)
	) {
	print "Usage: $0 -H host \n\t".
			"--mtwilson-url http://127.0.0.1:8181 --asset-tag-url http://127.0.0.1:1700 \n\t".
			"[-w <seconds>] [-c <seconds>]\n";
#	print "Usage 2: $0 \n\t".
#			"-p 'first plugin command with %TOKEN1% embedded' \n\t".
#			"-p 'second plugin command with %TOKEN1% embedded' \n\t".
#			"[-w <seconds1>,<seconds2>] [-c <seconds1>,<seconds2>] \n";
	exit $status{UNKNOWN};
}

# determine thresholds
if( scalar(@alarm_times) == 1 ) {
	$default_timeout = shift(@alarm_times);
}


# create the report object
my $report = new PluginReport;
my @report_plugins = (); # populated later with either (smtp,imap) or (plugin1,plugin2,...)
my $time_start = time; 

		eval {
			local $SIG{ALRM} = sub { die "exceeded timeout $plugin_timeout seconds\n" }; # NB: \n required, see `perldoc -f alarm`
			alarm $timeout;
            # make a call to mt wilson to check the trust status of this host
            
			my $output = `$p`;
			chomp $output;
			if( $output !~ m/OK|WARNING|CRITICAL/ ) {
				print "EMAIL DELIVERY UNKNOWN - plugin $i error: $output\n";
				print "Plugin $i: $p\n" if $verbose;
				# record tokens in a file if option is enabled
				record_tokens($tokenfile,\@tokens,$time_start,undef,'UNKNOWN',$i,$output) if $tokenfile;
				exit $status{UNKNOWN};
			}
			if( $output =~ m/CRITICAL/ && $alert_plugins{$i} ) {
				print "EMAIL DELIVERY CRITICAL - plugin $i failed: $output\n";
				print "Plugin $i: $p" if $verbose;
				# record tokens in a file if option is enabled
				record_tokens($tokenfile,\@tokens,$time_start,undef,'CRITICAL',$i,$output) if $tokenfile;
				exit $status{CRITICAL};
			}
			if( $output =~ m/WARNING/ && $alert_plugins{$i} ) {
				print "EMAIL DELIVERY WARNING - plugin $i warning: $output\n";
				print "Plugin $i: $p\n" if $verbose;
				# record tokens in a file if option is enabled
				record_tokens($tokenfile,\@tokens,$time_start,undef,'WARNING',$i,$output) if $tokenfile;
				exit $status{WARNING};
			}
			$report->{"plugin".$i} = $output;
			alarm 0;
		};
		if( $@ ) {
			print "EMAIL DELIVERY CRITICAL - Could not run plugin $i: $@\n";
			exit $status{CRITICAL};	
		}


# calculate elapsed time and issue warnings
my $time_end = time;
my $elapsedtime = $time_end - $time_start;
$report->{seconds} = $elapsedtime;

my @warning = ();
my @critical = ();

push @warning, "most recent received $report->{delay} seconds ago" if( defined($report->{delay}) && $report->{delay} > $delay_warn );
push @critical, "most recent received $report->{delay} seconds ago" if( defined($report->{delay}) && $report->{delay} > $delay_crit );
push @warning, "no emails found" if( !defined($report->{delay}) );

# print report and exit with known status
my $perf_data = "delay=".$report->{delay}."s;$delay_warn;$delay_crit;0 elapsed=".$report->{seconds}."s"; 
my $short_report = $report->text(qw/seconds delay/) . " | $perf_data";
my $long_report = join("", map { "$_: $report->{$_}\n" } @report_plugins );
if( scalar @critical ) {
	my $alerts = join(", ", @critical);
	print "EMAIL DELIVERY CRITICAL - $alerts; $short_report\n";
	print $long_report if $verbose;
	exit $status{CRITICAL};
}
if( scalar @warning ) {
	my $alerts = join(", ", @warning);
	print "EMAIL DELIVERY WARNING - $alerts; $short_report\n";
	print $long_report if $verbose;
	exit $status{WARNING};
}
print "EMAIL DELIVERY OK - $short_report\n";
print $long_report if $verbose;
exit $status{OK};

# utility to load required modules. exits if unable to load one or more of the modules.
sub load_modules {
	my @missing_modules = ();
	foreach( @_ ) {
		eval "require $_";
		push @missing_modules, $_ if $@;	
	}
	if( @missing_modules ) {
		print "Missing perl modules: @missing_modules\n";
		return 0;
	}
	return 1;
}

# wraps argument in single-quotes and escapes any single-quotes in the argument
sub shellquote {
	my $str = shift || "";
	$str =~ s/\'/\'\\\'\'/g;
	return "'$str'";
}


# NAME
#	PluginReport
# SYNOPSIS
#	$report = new PluginReport;
#   $report->{label1} = "value1";
#   $report->{label2} = "value2";
#	print $report->text(qw/label1 label2/);
package PluginReport;

sub new {
	my ($proto,%p) = @_;
	my $class = ref($proto) || $proto;
	my $self  = bless {}, $class;
	$self->{$_} = $p{$_} foreach keys %p;
	return $self;
}

sub text {
	my ($self,@labels) = @_;
	my @report = map { "$self->{$_} $_" } grep { defined $self->{$_} } @labels;
	my $text = join(", ", @report);
	return $text;
}

package main;
1;

__END__

=pod

=head1 NAME

check_email_delivery - sends email and verifies delivery

=head1 SYNOPSIS

 check_email_delivery -vV
 check_email_delivery --usage
 check_email_delivery --help

=head1 OPTIONS

=over

=item --warning <seconds>[,<smtp_seconds>,<imap_seconds>]

Exit with WARNING if the most recent email found is older than <seconds>. The
optional <smtp_seconds> and <imap_seconds> parameters will be passed on to the
included plugins that are used for those tasks. If they are not
given then they will not be passed on and the default for that plugin will apply.
Also known as: -w <seconds>[,<send>[,<recv>]]

When using the --plugin option, only one parameter is supported (-w <seconds>) and it will apply
to the entire process. You can specify a warning threshold specific to each plugin in the 
plugin command line. 

When using the --plugin option, no measuring of "most recent email" is done because we would
not know how to read this information from receive plugins. This may be addressed in future versions.

=item --critical <seconds>[,<smtp_seconds>,<imap_seconds>]

Exit with CRITICAL if the most recent email found is older than <seconds>. The
optional <smtp_seconds> and <imap_seconds> parameters will be passed on to the
included plugins that are used for those tasks. If they are not
given then they will not be passed on and the default for that plugin will apply.
Also known as: -c <seconds>[,<send>[,<recv>]]

When using the --plugin option, only one parameter is supported (-c <seconds>) and it will apply
to the entire process. You can specify a critical threshold specific to each plugin in the 
plugin command line. 

When using the --plugin option, no measuring of "most recent email" is done because we would
not know how to read this information from receive plugins. This may be addressed in future versions.

=item --timeout <seconds>

=item --timeout <smtp_seconds>,<imap_seconds>

=item --timeout <plugin1_seconds>,<plugin2_seconds>,...

Exit with CRITICAL if the plugins do not return a status within the specified number of seconds.
When only one parameter is used, it applies to each plugin. When multiple parameters are used
(separated by commas) they apply to plugins in the same order the plugins were specified on the
command line. When using --timeout but not the --plugin option, the first parameter is for 
check_smtp_send and the second is for check_imap_receive. 

=item --alert <pluginN>

Exit with WARNING or CRITICAL only if a warning or error (--warning, --critical, or --timeout)
occurs for specified plugins. If a warning or error occurs for non-specified plugins that run
BEFORE the specified plugins, the exit status will be UNKNOWN.  If a warning of error occurs
for non-specified plugins that run AFTER the specified plugins, the exit status will not be
affected. 

You would use this option if you are using check_email_delivery with the --plugin option and
the plugins you configure each use different servers, for example different SMTP and IMAP servers.
By default, if you do not use the --alert option, if anything goes wrong during the email delivery
check, a WARNING or CRITICAL alert will be issued. This means that if you define check_email_delivery
for the SMTP server only and the IMAP server fails, Nagios will alert you for the SMTP server which
would be misleading. If you define it for both the SMTP server and IMAP server and just one of them
fails, Nagios will alert you for both servers, which would still be misleading.  If you have this
situation, you may want to use the --alert option. You define the check_email_delivery check for
both servers:  for the SMTP server (first plugin) you use --alert 1, and for for the IMAP server
(second plugin) you use --alert 2. When check_email_delivery runs with --alert 1 and the SMTP
server fails, you will get the appropriate alert. If the IMAP server fails it will not affect the
status. When check_email_delivery runs with --alert 2 and the SMTP server fails, you will get the
UNKNOWN return code. If the IMAP server generates an alert you will get a WARNING or CRITICAL as
appropriate. 

You can repeat this option to specify multiple plugins that should cause an alert.
Do this if you have multiple plugins on the command line but some of them involve the same server.

See also: --plugin.
Also known as: -A <pluginN>


=item --wait <seconds>[,<seconds>,...]

How long to wait between sending the message and checking that it was received. View default with
the -vV option.

When using the --plugin option, you can specify as many wait-between times as you have plugins
(minus the last plugin, because it makes no sense to wait after running the last one). For
example, if you use the --plugin option twice to specify an SMTP plugin and an IMAP plugin, and
you want to wait 5 seconds between sending and receiving, then you would specify --wait 5. A second
example, if you are using the --plugin option three times, then specifying -w 5 will wait 5 seconds
between the second and third plugins also. You can specify a different wait time
of 10 seconds between the second and third plugins, like this:  -w 5,10. 

=item --hostname <server>

Address or name of the SMTP and IMAP server. Examples: mail.server.com, localhost, 192.168.1.100.
Also known as: -H <server>

=item --smtp-server <server>

Address or name of the SMTP server. Examples: smtp.server.com, localhost, 192.168.1.100.
Using this option overrides the hostname option.

=item --smtp-port <number>

Service port on the SMTP server. Default is 25.

=item --smtp-username <username>

=item --smtp-password <password>

Username and password to use when connecting to the SMTP server with the TLS option.
Use these options if the SMTP account has a different username/password than the
IMAP account you are testing. These options take precendence over the --username and
the --password options.

These are shell-escaped; special characters are ok. 

=item --imap-server <server>

Address or name of the IMAP server. Examples: imap.server.com, localhost, 192.168.1.100.
Using this option overrides the hostname option.

=item --imap-port <number>

Service port on the IMAP server. Default is 143. If you use SSL the default is 993.

=item --imap-username <username>

=item --imap-password <password>

Username and password to use when connecting to the IMAP server.
Use these options if the IMAP account has a different username/password than the
SMTP account you are testing. These options take precendence over the --username and
the --password options.

These are shell-escaped; special characters are ok. 

=item --username <username>

=item --password <password>

Username and password to use when connecting to IMAP server. 
Also known as: -U <username> -P <password>

Also used as the username and password for SMTP when the TLS option is enabled.
To specify a separate set of credentials for SMTP authentication, see the
options --smtp-username and --smtp-password.

=item --imap-check-interval <seconds>

How long to wait between polls of the imap-server for the specified mail. Default is 5 seconds.

=item --imap-retries <times>

How many times to poll the imap-server for the mail, before we give up. Default is 10. 

=item --body <message>

Use this option to specify the body of the email message.

=item --header <header>

Use this option to set an arbitrary header in the message. You can use it multiple times.

=item --mailto recipient@your.net

You can send a message to multiple recipients by repeating this option or by separating
the email addresses with commas (no whitespace allowed): 

$ check_email_delivery ... --mailto recipient@your.net,recipient2@your.net --mailfrom sender@your.net 

This argument is shell-escaped; special characters or angle brackets around the address are ok. 

=item --mailfrom sender@your.net

Use this option to set the "from" address in the email.

=item --imapssl
=item --noimapssl

Use this to enable or disable SSL for the IMAP plugin. 

This argument is shell-escaped; special characters or angle brackets around the address are ok. 

=item --smtptls
=item --nosmtptls

Use this to enable or disable TLS/AUTH for the SMTP plugin. 

=item --libexec

Use this option to set the path of the Nagios libexec directory. The default is
/usr/local/nagios/libexec. This is where this plugin looks for the SMTP and IMAP
plugins that it depends on.

=item --plugin <command>

This is a new option introduced in version 0.5 of the check_email_delivery plugin.
It frees the plugin from depending on specific external plugins and generalizes the
work done to determine that the email loop is operational. When using the --plugin
option, the following options are ignored: libexec, imapssl, smtptls, hostname, 
username, password, smtp*, imap*, mailto, mailfrom, body, header, search.

Use this option multiple times to specify the complete trip. Typically, you would use
this twice to specify plugins for SMTP and IMAP, or SMTP and POP3.

The output will be success if all the plugins return success. Each plugin should be a
standard Nagios plugin. 

A random token will be automatically generated and passed to each plugin specified on
the command line by substituting the string %TOKEN1%. 

Example usage:

 command_name check_email_delivery
 command_line check_email_delivery
 --plugin "$USER1$/check_smtp_send -H $ARG1$ --mailto recipient@your.net --mailfrom sender@your.net --header 'Subject: Nagios Test %TOKEN1%.'"
 --plugin "$USER1$/check_imap_receive -H $ARG1$ -U $ARG1$ -P $ARG2$ -s SUBJECT -s 'Nagios Test %TOKEN1%.'"

This technique allows for a lot of flexibility in configuring the plugins that test
each part of your email delivery loop. 

See also: --token.
Also known as: -p <command>

=item --token <format>

This is a new option introduced in version 0.5 of the check_email_delivery plugin.
It can be used in conjunction with --plugin to control the tokens that are generated
and passed to the plugins, like %TOKEN1%.

Use this option multiple times to specify formats for different tokens. For example,
if you want %TOKEN1% to consist of only alphabetical characters but want %TOKEN2% to
consist of only digits, then you might use these options: --token aaaaaa --token nnnnn

Any tokens used in your plugin commands that have not been specified by --token <format> 
will default to --token U-X-Y

Token formats:
a - alpha character (a-z)
n - numeric character (0-9)
c - alphanumeric character (a-z0-9)
h - hexadecimal character (0-9a-f)
U - unix time, seconds from epoch. eg 1193012441
X - a word from the pgp even list. eg aardvark
Y - a word from the pgp odd list. eg adroitness

Caution: It has been observed that some IMAP servers do not handle underscores well in the
search criteria. For best results, avoid using underscores in your tokens. Use hyphens or commas instead. 

See also: --plugin.
Also known as: -T <format>

The PGP word list was obtained from http://en.wikipedia.org/wiki/PGP_word_list

=item --file <file>

Save (append) status information into the given tab-delimited file. Format used:

 token	start-time	end-time	status	plugin-num	output

Note: format may change in future versions and may become configurable.

This option available as of version 0.6.2.

Also known as: -F <file>

=item --hires

Use the Time::HiRes module to measure time, if available.

=item --verbose

Display additional information. Useful for troubleshooting. Use together with --version to see the default
warning and critical timeout values.
Also known as: -v

=item --version

Display plugin version and exit.
Also known as: -V

=item --help

Display this documentation and exit. Does not work in the ePN version. 
Also known as: -h

=item --usage

Display a short usage instruction and exit. 

=back

=head1 EXAMPLES

=head2 Send a message with custom headers

$ check_email_delivery -H mail.server.net --mailto recipient@your.net --mailfrom sender@your.net 
--username recipient --password secret

EMAIL DELIVERY OK - 1 seconds

=head2 Set warning and critical timeouts for receive plugin only:

$ check_email_delivery -H mail.server.net --mailto recipient@your.net --mailfrom sender@your.net 
--username recipient --password secret -w ,,5 -c ,,15

EMAIL DELIVERY OK - 1 seconds

=head1 EXIT CODES

Complies with the Nagios plug-in specification:
 0		OK			The plugin was able to check the service and it appeared to be functioning properly
 1		Warning		The plugin was able to check the service, but it appeared to be above some "warning" threshold or did not appear to be working properly
 2		Critical	The plugin detected that either the service was not running or it was above some "critical" threshold
 3		Unknown		Invalid command line arguments were supplied to the plugin or the plugin was unable to check the status of the given hosts/service

=head1 NAGIOS PLUGIN NOTES

Nagios plugin reference: http://nagiosplug.sourceforge.net/developer-guidelines.html

This plugin does NOT use Nagios DEFAULT_SOCKET_TIMEOUT (provided by utils.pm as $TIMEOUT) because
the path to utils.pm must be specified completely in this program and forces users to edit the source
code if their install location is different (if they realize this is the problem). You can view
the default timeout for this module by using the --verbose and --version options together.  The
short form is -vV.

Other than that, it attempts to follow published guidelines for Nagios plugins.

=head1 CHANGES

 Wed Oct 29 13:08:00 PST 2005
 + version 0.1

 Wed Nov  9 17:16:09 PST 2005
 + updated arguments to check_smtp_send and check_imap_receive
 + added eval/alarm block to implement -c option
 + added wait option to adjust sleep time between smtp and imap calls
 + added delay-warn and delay-crit options to adjust email delivery warning thresholds
 + now using an inline PluginReport package to generate the report
 + copyright notice and GNU GPL
 + version 0.2

 Thu Apr 20 14:00:00 CET 2006 (by Johan Nilsson <johann (at) axis.com>)
 + version 0.2.1
 + corrected bug in getoptions ($imap_server would never ever be set from command-line...)
 + will not make $smtp_server and $imap_server == $host if they're defined on commandline 
 + added support for multiple polls of imap-server, with specified intervals
 + changed default behaviour in check_imap_server (searches for the specific id in subject and deletes mails found)
 + increased default delay_warn from 65 seconds to 95 seconds 

 Thu Apr 20 16:00:00 PST 2006 (by Geoff Crompton <geoff.crompton@strategicdata.com.au>)
 + fixed a bug in getoptions
 + version 0.2.2

 Tue Apr 24 21:17:53 PDT 2007
 + now there is an alternate version (same but without embedded perl POD) that is compatible with the new new embedded-perl Nagios feature
 + version 0.2.3

 Fri Apr 27 20:32:53 PDT 2007 
 + documentation now mentions every command-line option accepted by the plugin, including abbreviations
 + changed connection error to display timeout only if timeout was the error
 + default IMAP plugin is libexec/check_imap_receive (also checking for same but with .pl extension)
 + default SMTP plugin is libexec/check_smtp_send (also checking for same but with .pl extension)
 + removed default values for SMTP port and IMAP port to allow those plugins to set the defaults; so current behavior stays the same and will continue to make sense with SSL
 + version 0.3

 Thu Oct 11 10:00:00 EET 2007 (by Timo Virtaneva <timo (at) virtaneva dot com>
 + Changed the header and the search criteria so that the same email-box can be used for all smtp-servers
 + version 0.3.1

 Sun Oct 21 11:01:03 PDT 2007
 + added support for TLS options to the SMTP plugin
 + version 0.4

 Sun Oct 21 16:17:14 PDT 2007
 + added support for arbitrary plugins to send and receive mail (or anthing else!). see the --plugin option.
 + version 0.5

 Tue Dec  4 07:36:20 PST 2007
 + added --usage option because the official nagios plugins have both --help and --usage
 + added --timeout option to match the official nagios plugins
 + shortcut option for --token is now -T to avoid clash with standard shortcut -t for --timeout
 + fixed some minor pod formatting issues for perldoc
 + version 0.5.1

 Sat Dec 15 07:39:59 PST 2007
 + improved compatibility with Nagios embedded perl (ePN)
 + version 0.5.2

 Thu Jan 17 20:27:36 PST 2008 (by Timo Virtaneva <timo (at) virtaneva dot com> on Thu Oct 11 10:00:00 EET 2007)
 + Changed the header and the search criteria so that the same email-box can be used for all smtp-servers
 + version 0.5.3

 Mon Jan 28 22:11:02 PST 2008
 + fixed a bug, smtp-password and imap-password are now string parameters
 + added --alert option to allow selection of which plugin(s) should cause a WARNING or CRITICAL alert
 + version 0.6

 Mon Feb 11 19:09:37 PST 2008
 + fixed a bug for embedded perl version, variable "%status" will not stay shared in load_modules
 + version 0.6.1

 Mon May 26 10:39:19 PDT 2008
 + added --file option to allow plugin to record status information into a tab-delimited file
 + changed default token from U_X_Y to U-X-Y 
 + version 0.6.2

 Wed Jan 14 08:29:35 PST 2009
 + fixed a bug that the --header parameter was not being passed to the smtp plugin.
 + version 0.6.3

 Mon Jun  8 15:43:48 PDT 2009
 + added performance data for use with PNP4Nagios! (thanks to Ben Ritcey for the patch)
 + version 0.6.4

 Wed Sep 16 07:10:10 PDT 2009
 + added elapsed time in seconds to performance data
 + version 0.6.5

 Fri Oct  8 19:48:44 PDT 2010
 + fixed uniform IMAP and SMTP username and password bug (thanks to Micle Moerenhout for pointing it out)
 + version 0.6.6

 Mon Jan  3 08:24:23 PST 2011
 + added shell escaping for smtp-username, smtp-password, mailto, mailfrom, imap-username, and imap-password arguments
 + version 0.7.0

 Fri May  6 08:35:09 AST 2011
 + added --hires option to enable use of Time::Hires if available
 + version 0.7.1

 Sun Jun 12 17:17:06 AST 2011
 + added --imap-mailbox option to pass through to check_imap_receive --mailbox option
 + added --ssl option to conveniently enable both --smtp-tls and --imap-ssl 
 + version 0.7.2

=head1 AUTHOR

Jonathan Buhacoff <jonathan@buhacoff.net>

=head1 COPYRIGHT AND LICENSE

 Copyright (C) 2005-2011 Jonathan Buhacoff

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 http://www.gnu.org/licenses/gpl.txt

=cut
