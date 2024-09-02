# EverShare Plugin

<p align="center">
  <img src="https://i.imgur.com/m5Nwigv.png" alt="EverShare Logo" width="300"/>
</p>

## Description

EverShare is a Minecraft plugin that allows players to generate referral codes, track hits, and integrate web-based referral information. It features a web server for displaying referral data and a configuration-driven system for easy customization.

## Features

- **Referral Code Generation**: Automatically generate and manage referral codes for players.
- **Referral Tracking**: Track how many times a referral link has been accessed.
- **Web Interface**: Provides a web interface to view referral information, including referral codes and hit counts.
- **Customizable Messages**: Easily configure and customize the messages displayed on the web interface.
- **Favicon Support**: Automatically download and use a favicon for the web interface.

## Installation

1. **Download the Plugin:**

   Download the latest release of EverShare from the [releases page](https://github.com/UnStackss/EverShare/releases).

2. **Add to Server:**

   Place the downloaded `.jar` file in the `plugins` folder of your Minecraft server.

3. **Restart the Server:**

   Restart your Minecraft server to load the plugin.

4. **Configure the Plugin:**

   Edit the `config.yml` file in the `plugins/EverShare` folder to set your preferences, including database connection details and web server settings.

## Usage

### Commands

- **/referralinfo**: Displays your referral code, referral link, and the number of hits. This command requires the `referralinfo.use` permission.

### Web Interface

The web interface can be accessed at the URL specified in the `config.yml` file. It will display information about the referral code, including the referral owner's name, total hits, and server IP.

### Customizing Messages

The messages displayed in the web interface can be customized by editing the `languages/en_us.json` file in the `plugins/EverShare` folder.

## Troubleshooting

- **Plugin Not Working**: Ensure that the plugin is correctly placed in the `plugins` folder and that your server is restarted.
- **Database Issues**: Verify your database connection details in the `config.yml` file.
- **Web Interface Issues**: Ensure the web server settings are correctly configured and that the favicon URL is accessible.

## License

This plugin is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Contributing

If you would like to contribute to the development of EverShare, please fork the repository and submit a pull request. Contributions are always welcome!

## Contact

For any questions or support, please contact [support@unstackss.dev] or open an issue on the [GitHub issues page](https://github.com/UnStackss/EverShare/issues).
